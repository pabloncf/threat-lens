package com.pabloncf.threatlens.detection;

import com.pabloncf.threatlens.common.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A raw event flagged by a detector, before AI triage.
 */
@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private SecurityEventType eventType;

    @Column(name = "source_ip", nullable = false)
    private String sourceIp;

    @Column(name = "request_uri", nullable = false)
    private String requestUri;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(name = "raw_details")
    private String rawDetails;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SecurityEvent() {
        // JPA
    }

    public SecurityEvent(
            SecurityEventType eventType,
            String sourceIp,
            String requestUri,
            String httpMethod,
            int score,
            Severity severity,
            String rawDetails,
            Instant detectedAt) {
        this.eventType = eventType;
        this.sourceIp = sourceIp;
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.score = score;
        this.severity = severity;
        this.rawDetails = rawDetails;
        this.detectedAt = detectedAt;
    }

    public UUID getId() {
        return id;
    }

    public SecurityEventType getEventType() {
        return eventType;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public int getScore() {
        return score;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getRawDetails() {
        return rawDetails;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
