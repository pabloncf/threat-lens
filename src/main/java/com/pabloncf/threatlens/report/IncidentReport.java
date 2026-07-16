package com.pabloncf.threatlens.report;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * The AI-triage output for a {@link SecurityEvent}: an OWASP-aligned classification and a
 * remediation recommendation. {@code aiGenerated} distinguishes a real LLM response from the
 * fallback template used when the Claude API is unreachable.
 */
@Entity
@Table(name = "incident_reports")
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "security_event_id", nullable = false, unique = true)
    private SecurityEvent securityEvent;

    @Column(nullable = false)
    private String classification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String recommendation;

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IncidentReport() {
        // JPA
    }

    public IncidentReport(
            SecurityEvent securityEvent,
            String classification,
            Severity severity,
            String recommendation,
            boolean aiGenerated) {
        this.securityEvent = securityEvent;
        this.classification = classification;
        this.severity = severity;
        this.recommendation = recommendation;
        this.aiGenerated = aiGenerated;
    }

    public UUID getId() {
        return id;
    }

    public SecurityEvent getSecurityEvent() {
        return securityEvent;
    }

    public String getClassification() {
        return classification;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
