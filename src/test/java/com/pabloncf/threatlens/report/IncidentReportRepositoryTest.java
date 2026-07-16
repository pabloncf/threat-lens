package com.pabloncf.threatlens.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.detection.SecurityEventRepository;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class IncidentReportRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Test
    void savesAndRetrievesAnIncidentReportLinkedToItsSecurityEvent() {
        // Arrange
        SecurityEvent event = securityEventRepository.save(new SecurityEvent(
                SecurityEventType.XSS,
                "198.51.100.7",
                "/comments",
                "POST",
                72,
                Severity.MEDIUM,
                "<script>alert(1)</script>",
                Instant.now()));

        IncidentReport report = new IncidentReport(
                event,
                "A03:2021-Injection",
                Severity.MEDIUM,
                "Sanitize and encode user input before rendering.",
                true);

        // Act
        IncidentReport saved = incidentReportRepository.save(report);
        Optional<IncidentReport> found =
                incidentReportRepository.findBySecurityEventId(event.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getSecurityEvent().getId()).isEqualTo(event.getId());
        assertThat(found.get().getClassification()).isEqualTo("A03:2021-Injection");
        assertThat(found.get().isAiGenerated()).isTrue();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }
}
