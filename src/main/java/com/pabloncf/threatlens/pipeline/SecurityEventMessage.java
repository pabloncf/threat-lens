package com.pabloncf.threatlens.pipeline;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.time.Instant;

/**
 * Kafka payload combining a {@code DetectionRequest}'s context with the {@code
 * DetectionResult} it produced. Serialized as JSON.
 */
public record SecurityEventMessage(
        String sourceIp,
        String requestUri,
        String httpMethod,
        SecurityEventType eventType,
        int score,
        Severity severity,
        String reason,
        Instant detectedAt) {
}
