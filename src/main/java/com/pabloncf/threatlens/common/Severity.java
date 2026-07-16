package com.pabloncf.threatlens.common;

/**
 * Severity level shared by {@code SecurityEvent} and {@code IncidentReport}.
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /**
     * Maps a 0-100 detection score to a severity level: 0-39 LOW, 40-69 MEDIUM, 70-89 HIGH,
     * 90-100 CRITICAL.
     */
    public static Severity fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100: " + score);
        }
        if (score >= 90) {
            return CRITICAL;
        }
        if (score >= 70) {
            return HIGH;
        }
        if (score >= 40) {
            return MEDIUM;
        }
        return LOW;
    }
}
