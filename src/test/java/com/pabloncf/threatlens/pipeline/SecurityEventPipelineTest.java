package com.pabloncf.threatlens.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.detection.SecurityEventRepository;
import com.pabloncf.threatlens.detection.SecurityEventType;
import com.pabloncf.threatlens.triage.ClaudeTriageClient;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

/**
 * Exercises the full Kafka pipeline end to end against real infrastructure: a message
 * published through {@link SecurityEventProducer} must arrive at {@link SecurityEventConsumer}
 * and land in Postgres via {@link SecurityEventRepository}. {@link ClaudeTriageClient} is
 * mocked - this test's scope is the Kafka pipeline, not AI triage, and the test event's score
 * (85, HIGH) would otherwise trigger a real, unmocked call to the Claude API.
 */
@SpringBootTest
@Testcontainers
class SecurityEventPipelineTest {

    @MockitoBean
    private ClaudeTriageClient claudeTriageClient;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static final ConfluentKafkaContainer kafka =
            new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.1");

    @Autowired
    private SecurityEventProducer producer;

    @Autowired
    private SecurityEventRepository repository;

    @Test
    void publishedMessageIsPersistedAsASecurityEvent() {
        // Arrange
        String sourceIp = "203.0.113.9";
        Instant detectedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        SecurityEventMessage message = new SecurityEventMessage(
                sourceIp,
                "/login",
                "POST",
                SecurityEventType.BRUTE_FORCE,
                85,
                Severity.HIGH,
                "9 requests to /login from 203.0.113.9 within PT1M (threshold 5)",
                detectedAt);

        // Act
        producer.publish(message);

        // Assert
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<SecurityEvent> events = repository.findAll().stream()
                    .filter(event -> event.getSourceIp().equals(sourceIp))
                    .toList();
            assertThat(events).hasSize(1);
            SecurityEvent event = events.getFirst();
            assertThat(event.getEventType()).isEqualTo(SecurityEventType.BRUTE_FORCE);
            assertThat(event.getRequestUri()).isEqualTo("/login");
            assertThat(event.getScore()).isEqualTo(85);
            assertThat(event.getSeverity()).isEqualTo(Severity.HIGH);
        });
    }
}
