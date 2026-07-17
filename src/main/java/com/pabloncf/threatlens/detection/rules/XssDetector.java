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
 * Flags common cross-site scripting patterns: script tags, the {@code javascript:} URI
 * scheme, inline event handlers, and cookie theft via {@code document.cookie}.
 */
@Component
public class XssDetector implements SecurityDetector {

    private static final List<WeightedPattern> PATTERNS = List.of(
            new WeightedPattern(Pattern.compile("(?i)<script\\b"), 50, "script tag"),
            new WeightedPattern(Pattern.compile("(?i)javascript:"), 40, "javascript: URI"),
            new WeightedPattern(Pattern.compile("(?i)\\bon\\w+\\s*="), 40, "inline event handler"),
            new WeightedPattern(Pattern.compile("(?i)document\\.cookie"), 30, "document.cookie access"));

    @Override
    public SecurityEventType supportedType() {
        return SecurityEventType.XSS;
    }

    @Override
    public Optional<DetectionResult> detect(DetectionRequest request) {
        return PatternScorer.score(request.payload(), PATTERNS)
                .map(match -> new DetectionResult(
                        SecurityEventType.XSS,
                        match.score(),
                        Severity.fromScore(match.score()),
                        match.reason()));
    }
}
