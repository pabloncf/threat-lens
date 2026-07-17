package com.pabloncf.threatlens.triage;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Structured output schema for the triage prompt. {@code severity} is a plain string (not the
 * {@code Severity} enum directly) since the model's raw answer is parsed defensively rather
 * than trusted to match an enum constant exactly.
 */
public record ClaudeTriageResponse(
        @JsonPropertyDescription("OWASP Top 10 / CWE-aligned classification, e.g. 'A03:2021-Injection'")
                String classification,
        @JsonPropertyDescription("One of: LOW, MEDIUM, HIGH, CRITICAL") String severity,
        @JsonPropertyDescription(
                        "Remediation guidance only - never exploit code or attack instructions. Reference OWASP/CWE where relevant.")
                String recommendation) {
}
