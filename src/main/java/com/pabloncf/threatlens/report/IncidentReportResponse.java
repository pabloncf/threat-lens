package com.pabloncf.threatlens.report;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-model DTO flattening an {@link IncidentReport} with its {@code SecurityEvent} into a
 * single response - the API never exposes JPA entities directly.
 */
public record IncidentReportResponse(
        UUID id,
        UUID securityEventId,
        SecurityEventType eventType,
        String sourceIp,
        String requestUri,
        int score,
        Severity severity,
        String classification,
        String recommendation,
        boolean aiGenerated,
        Instant createdAt) {

    public static IncidentReportResponse from(IncidentReport report) {
        var event = report.getSecurityEvent();
        return new IncidentReportResponse(
                report.getId(),
                event.getId(),
                event.getEventType(),
                event.getSourceIp(),
                event.getRequestUri(),
                event.getScore(),
                report.getSeverity(),
                report.getClassification(),
                report.getRecommendation(),
                report.isAiGenerated(),
                report.getCreatedAt());
    }
}
