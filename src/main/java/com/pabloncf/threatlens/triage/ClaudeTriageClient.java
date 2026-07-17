package com.pabloncf.threatlens.triage;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wraps the Claude API for incident triage. Callers are responsible for redacting sensitive
 * data before it reaches this class and for handling failures (the Claude API is never a hard
 * dependency for detection - see {@code IncidentTriageService}'s fallback path).
 */
@Component
public class ClaudeTriageClient {

    private static final String SYSTEM_PROMPT =
            """
            You are a security triage assistant for ThreatLens, a defensive security platform.
            Classify the detected event using an OWASP Top 10 / CWE-aligned category, assess its
            severity, and provide remediation guidance.

            Never include exploit code, attack payloads, or step-by-step instructions for
            carrying out an attack. Recommendations must describe defensive remediation only,
            referencing OWASP or CWE identifiers where relevant.
            """;

    private final AnthropicClient client;
    private final String model;

    public ClaudeTriageClient(@Value("${threatlens.anthropic.api-key}") String apiKey, TriageProperties properties) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.model = properties.model();
    }

    public ClaudeTriageResponse triage(String eventType, int score, String severity, String redactedDetails) {
        StructuredMessageCreateParams<ClaudeTriageResponse> params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024L)
                .system(SYSTEM_PROMPT)
                .outputConfig(ClaudeTriageResponse.class)
                .addUserMessage(
                        """
                        Detected event:
                        - type: %s
                        - score: %d/100
                        - severity: %s
                        - details: %s
                        """
                                .formatted(eventType, score, severity, redactedDetails))
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(StructuredTextBlock::text)
                .orElseThrow(() -> new IllegalStateException("Claude returned no structured text block"));
    }
}
