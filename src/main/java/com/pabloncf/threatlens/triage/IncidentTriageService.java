package com.pabloncf.threatlens.triage;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.report.IncidentReport;
import com.pabloncf.threatlens.report.IncidentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gates events into AI triage by severity (cost control - only suspicious-enough events reach
 * the LLM) and turns the result into a persisted {@link IncidentReport}. The Claude API is
 * never a hard dependency: any failure produces a fallback report ({@code aiGenerated = false})
 * instead of losing the event's triage entirely.
 */
@Service
public class IncidentTriageService {

    private static final Logger log = LoggerFactory.getLogger(IncidentTriageService.class);
    private static final String FALLBACK_RECOMMENDATION =
            "Automated triage unavailable - review this event manually.";
    private static final String FALLBACK_CLASSIFICATION = "Unclassified";

    private final ClaudeTriageClient claudeTriageClient;
    private final SensitiveDataRedactor redactor;
    private final IncidentReportRepository repository;
    private final TriageProperties properties;

    public IncidentTriageService(
            ClaudeTriageClient claudeTriageClient,
            SensitiveDataRedactor redactor,
            IncidentReportRepository repository,
            TriageProperties properties) {
        this.claudeTriageClient = claudeTriageClient;
        this.redactor = redactor;
        this.repository = repository;
        this.properties = properties;
    }

    public void triageIfSuspicious(SecurityEvent event) {
        if (event.getSeverity().ordinal() < properties.minSeverity().ordinal()) {
            return;
        }
        repository.save(triage(event));
    }

    private IncidentReport triage(SecurityEvent event) {
        try {
            String redactedDetails = redactor.redact(event.getRawDetails());
            ClaudeTriageResponse response = claudeTriageClient.triage(
                    event.getEventType().name(), event.getScore(), event.getSeverity().name(), redactedDetails);
            Severity severity = parseSeverity(response.severity(), event.getSeverity());
            return new IncidentReport(event, response.classification(), severity, response.recommendation(), true);
        } catch (RuntimeException e) {
            log.warn("AI triage failed for event {}: {}", event.getId(), e.getMessage());
            return new IncidentReport(
                    event, FALLBACK_CLASSIFICATION, event.getSeverity(), FALLBACK_RECOMMENDATION, false);
        }
    }

    private Severity parseSeverity(String raw, Severity fallback) {
        try {
            return Severity.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return fallback;
        }
    }
}
