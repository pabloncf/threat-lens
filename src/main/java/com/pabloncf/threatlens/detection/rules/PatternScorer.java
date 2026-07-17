package com.pabloncf.threatlens.detection.rules;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shared scoring logic for regex-based detectors: sum the weights of every matched pattern,
 * cap at 100, and join the matched descriptions into a reason string.
 */
final class PatternScorer {

    private PatternScorer() {
    }

    static Optional<Match> score(String payload, List<WeightedPattern> patterns) {
        if (payload == null || payload.isEmpty()) {
            return Optional.empty();
        }

        List<WeightedPattern> matched = patterns.stream()
                .filter(p -> p.pattern().matcher(payload).find())
                .toList();

        if (matched.isEmpty()) {
            return Optional.empty();
        }

        int score = Math.min(100, matched.stream().mapToInt(WeightedPattern::weight).sum());
        String reason = matched.stream().map(WeightedPattern::description).collect(Collectors.joining(", "));
        return Optional.of(new Match(score, reason));
    }

    record Match(int score, String reason) {
    }
}
