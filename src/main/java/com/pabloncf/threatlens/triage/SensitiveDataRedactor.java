package com.pabloncf.threatlens.triage;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Scrubs credentials, tokens, and PII from event details before they reach the LLM. Detectors
 * today only emit pattern-name summaries (never raw attacker payloads), but this exists as a
 * safety net for whatever ends up in {@code SecurityEvent.rawDetails} - a detector added later
 * might not be as careful, and the ethical guardrails require this regardless.
 */
@Component
public class SensitiveDataRedactor {

    private record Rule(Pattern pattern, String replacement) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule(Pattern.compile("(?i)(password|passwd|pwd)\\s*[:=]\\s*\\S+"), "$1=[REDACTED]"),
            new Rule(Pattern.compile("(?i)(token|api[_-]?key|secret)\\s*[:=]\\s*\\S+"), "$1=[REDACTED]"),
            new Rule(Pattern.compile("(?i)authorization:\\s*bearer\\s+\\S+"), "authorization: Bearer [REDACTED]"),
            new Rule(Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+"), "[REDACTED_EMAIL]"));

    public String redact(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (Rule rule : RULES) {
            result = rule.pattern().matcher(result).replaceAll(rule.replacement());
        }
        return result;
    }
}
