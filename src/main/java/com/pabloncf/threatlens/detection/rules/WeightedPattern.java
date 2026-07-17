package com.pabloncf.threatlens.detection.rules;

import java.util.regex.Pattern;

/**
 * A single suspicious pattern a detector looks for, with its contribution to the score if
 * matched and a short human-readable description used in {@code DetectionResult.reason}.
 */
record WeightedPattern(Pattern pattern, int weight, String description) {
}
