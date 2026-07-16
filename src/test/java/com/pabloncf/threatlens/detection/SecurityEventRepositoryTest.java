package com.pabloncf.threatlens.detection;

import static org.assertj.core.api.Assertions.assertThat;

import com.pabloncf.threatlens.common.Severity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
class SecurityEventRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SecurityEventRepository repository;

    @Test
    void savesAndRetrievesASecurityEvent() {
        // Arrange
        Instant detectedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        SecurityEvent event = new SecurityEvent(
                SecurityEventType.SQL_INJECTION,
                "203.0.113.5",
                "/login",
                "POST",
                87,
                Severity.HIGH,
                "' OR 1=1 --",
                detectedAt);

        // Act
        SecurityEvent saved = repository.save(event);
        Optional<SecurityEvent> found = repository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEventType()).isEqualTo(SecurityEventType.SQL_INJECTION);
        assertThat(found.get().getSourceIp()).isEqualTo("203.0.113.5");
        assertThat(found.get().getScore()).isEqualTo(87);
        assertThat(found.get().getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(found.get().getDetectedAt()).isEqualTo(detectedAt);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }
}
