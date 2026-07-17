package com.pabloncf.threatlens.detection.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SqlInjectionDetectorTest {

    private final SqlInjectionDetector detector = new SqlInjectionDetector();

    @Test
    void flagsATautologyPattern() {
        // Arrange
        DetectionRequest request = requestWithPayload("username=admin' OR '1'='1");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().eventType()).isEqualTo(SecurityEventType.SQL_INJECTION);
        assertThat(result.get().score()).isEqualTo(40);
        assertThat(result.get().severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void flagsAUnionSelectAndCommentMarkerAsHighSeverity() {
        // Arrange
        DetectionRequest request = requestWithPayload(
                "id=1 UNION SELECT username, password FROM admins--");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().score()).isEqualTo(70);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
        assertThat(result.get().reason()).contains("UNION SELECT").contains("SQL comment marker");
    }

    @Test
    void flagsAStackedQueryWithCommentAsHighSeverity() {
        // Arrange
        DetectionRequest request = requestWithPayload("id=1'; DROP TABLE users;--");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().score()).isEqualTo(80);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void doesNotFlagOrdinaryText() {
        // Arrange
        DetectionRequest request = requestWithPayload("comment=I really enjoyed this article, thanks!");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void doesNotFlagAnEmptyPayload() {
        // Arrange
        DetectionRequest request = requestWithPayload("");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isEmpty();
    }

    private static DetectionRequest requestWithPayload(String payload) {
        return new DetectionRequest("203.0.113.5", "/search", "GET", payload);
    }
}
