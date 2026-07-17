package com.pabloncf.threatlens.detection.rules;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import com.pabloncf.threatlens.detection.SecurityDetector;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Flags common SQL injection patterns: tautologies, UNION-based extraction, comment markers
 * used to truncate the original query, and stacked queries.
 */
@Component
public class SqlInjectionDetector implements SecurityDetector {

    private static final List<WeightedPattern> PATTERNS = List.of(
            new WeightedPattern(
                    Pattern.compile("(?i)\\b(or|and)\\b\\s*['\"]?\\w+['\"]?\\s*=\\s*['\"]?\\w+['\"]?"),
                    40,
                    "tautology"),
            new WeightedPattern(Pattern.compile("(?i)union\\s+select"), 50, "UNION SELECT"),
            new WeightedPattern(Pattern.compile("(--|#)"), 20, "SQL comment marker"),
            new WeightedPattern(
                    Pattern.compile("(?i);\\s*(drop|delete|update|insert)\\b"), 60, "stacked query"));

    @Override
    public SecurityEventType supportedType() {
        return SecurityEventType.SQL_INJECTION;
    }

    @Override
    public Optional<DetectionResult> detect(DetectionRequest request) {
        return PatternScorer.score(request.payload(), PATTERNS)
                .map(match -> new DetectionResult(
                        SecurityEventType.SQL_INJECTION,
                        match.score(),
                        Severity.fromScore(match.score()),
                        match.reason()));
    }
}
