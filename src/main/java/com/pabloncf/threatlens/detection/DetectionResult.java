package com.pabloncf.threatlens.detection;

import com.pabloncf.threatlens.common.Severity;

/**
 * What a {@link SecurityDetector} produces when a request looks suspicious.
 *
 * @param reason concise description of the matched pattern(s), not the raw payload.
 */
public record DetectionResult(SecurityEventType eventType, int score, Severity severity, String reason) {
}
