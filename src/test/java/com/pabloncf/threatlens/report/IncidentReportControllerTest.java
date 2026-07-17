package com.pabloncf.threatlens.report;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.detection.SecurityEventType;
import com.pabloncf.threatlens.pipeline.DetectionFilter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test for the web layer only - the repository is mocked (its Postgres behavior is
 * already covered by the Phase 2 repository tests) and the security filter chain is disabled
 * ({@code addFilters = false}) since authentication itself is Spring Security's own tested
 * behavior, not this controller's. {@link DetectionFilter} is excluded from the slice's
 * component scan - it's picked up automatically as a servlet {@code Filter} bean but its own
 * dependencies (DetectionEngine, SecurityEventProducer) are unrelated to this controller.
 */
@WebMvcTest(
        controllers = IncidentReportController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DetectionFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class IncidentReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentReportRepository repository;

    @Test
    void listsIncidentReportsWithFlattenedEventContext() throws Exception {
        // Arrange
        IncidentReport report = incidentReport(Severity.HIGH);
        when(repository.findAllWithSecurityEvent(any()))
                .thenReturn(new PageImpl<>(List.of(report), PageRequest.of(0, 20), 1));

        // Act & Assert
        mockMvc.perform(get("/api/incident-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].classification").value("A03:2021-Injection"))
                .andExpect(jsonPath("$.content[0].sourceIp").value("203.0.113.5"))
                .andExpect(jsonPath("$.content[0].aiGenerated").value(true));
    }

    @Test
    void returnsAReportById() throws Exception {
        // Arrange
        IncidentReport report = incidentReport(Severity.CRITICAL);
        when(repository.findById(any())).thenReturn(Optional.of(report));

        // Act & Assert
        mockMvc.perform(get("/api/incident-reports/{id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("CRITICAL"));
    }

    @Test
    void returnsNotFoundForAMissingReport() throws Exception {
        // Arrange
        when(repository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/incident-reports/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    private static IncidentReport incidentReport(Severity severity) {
        SecurityEvent event = new SecurityEvent(
                SecurityEventType.SQL_INJECTION,
                "203.0.113.5",
                "/search",
                "GET",
                80,
                severity,
                "tautology",
                Instant.now());
        return new IncidentReport(event, "A03:2021-Injection", severity, "Use parameterized queries.", true);
    }
}
