package com.pabloncf.threatlens.triage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.detection.SecurityEventType;
import com.pabloncf.threatlens.report.IncidentReport;
import com.pabloncf.threatlens.report.IncidentReportRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IncidentTriageServiceTest {

    private final ClaudeTriageClient claudeTriageClient = mock(ClaudeTriageClient.class);
    private final IncidentReportRepository repository = mock(IncidentReportRepository.class);
    private final TriageProperties properties = new TriageProperties("claude-haiku-4-5", Severity.HIGH);
    private final IncidentTriageService service =
            new IncidentTriageService(claudeTriageClient, new SensitiveDataRedactor(), repository, properties);

    @Test
    void skipsTriageBelowTheSeverityThreshold() {
        // Arrange
        SecurityEvent event = securityEvent(Severity.MEDIUM);

        // Act
        service.triageIfSuspicious(event);

        // Assert
        verify(claudeTriageClient, never()).triage(anyString(), anyInt(), anyString(), anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void savesAnAiGeneratedReportOnSuccess() {
        // Arrange
        SecurityEvent event = securityEvent(Severity.HIGH);
        when(claudeTriageClient.triage(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(new ClaudeTriageResponse("A03:2021-Injection", "HIGH", "Use parameterized queries."));

        // Act
        service.triageIfSuspicious(event);

        // Assert
        ArgumentCaptor<IncidentReport> captor = ArgumentCaptor.forClass(IncidentReport.class);
        verify(repository).save(captor.capture());
        IncidentReport report = captor.getValue();
        assertThat(report.getClassification()).isEqualTo("A03:2021-Injection");
        assertThat(report.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(report.getRecommendation()).isEqualTo("Use parameterized queries.");
        assertThat(report.isAiGenerated()).isTrue();
    }

    @Test
    void savesAFallbackReportWhenClaudeFails() {
        // Arrange
        SecurityEvent event = securityEvent(Severity.CRITICAL);
        when(claudeTriageClient.triage(anyString(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("connection refused"));

        // Act
        service.triageIfSuspicious(event);

        // Assert
        ArgumentCaptor<IncidentReport> captor = ArgumentCaptor.forClass(IncidentReport.class);
        verify(repository).save(captor.capture());
        IncidentReport report = captor.getValue();
        assertThat(report.isAiGenerated()).isFalse();
        assertThat(report.getSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(report.getRecommendation()).contains("manually");
    }

    private static SecurityEvent securityEvent(Severity severity) {
        int score = switch (severity) {
            case LOW -> 20;
            case MEDIUM -> 50;
            case HIGH -> 80;
            case CRITICAL -> 95;
        };
        return new SecurityEvent(
                SecurityEventType.SQL_INJECTION,
                "203.0.113.5",
                "/search",
                "GET",
                score,
                severity,
                "tautology",
                Instant.now());
    }
}
