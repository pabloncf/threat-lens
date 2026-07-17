package com.pabloncf.threatlens.detection;

import static org.assertj.core.api.Assertions.assertThat;

import com.pabloncf.threatlens.common.Severity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DetectionEngineTest {

    private static final DetectionRequest REQUEST =
            new DetectionRequest("203.0.113.5", "/login", "POST", "irrelevant");

    @Test
    void collectsResultsOnlyFromDetectorsThatFired() {
        // Arrange
        SecurityDetector firing = fakeDetector(SecurityEventType.SQL_INJECTION,
                Optional.of(new DetectionResult(SecurityEventType.SQL_INJECTION, 80, Severity.HIGH, "matched")));
        SecurityDetector silent = fakeDetector(SecurityEventType.XSS, Optional.empty());
        DetectionEngine engine = new DetectionEngine(List.of(firing, silent));

        // Act
        List<DetectionResult> results = engine.detect(REQUEST);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().eventType()).isEqualTo(SecurityEventType.SQL_INJECTION);
    }

    @Test
    void returnsEmptyListWhenNoDetectorFires() {
        // Arrange
        DetectionEngine engine = new DetectionEngine(
                List.of(fakeDetector(SecurityEventType.SQL_INJECTION, Optional.empty())));

        // Act
        List<DetectionResult> results = engine.detect(REQUEST);

        // Assert
        assertThat(results).isEmpty();
    }

    private static SecurityDetector fakeDetector(SecurityEventType type, Optional<DetectionResult> result) {
        return new SecurityDetector() {
            @Override
            public SecurityEventType supportedType() {
                return type;
            }

            @Override
            public Optional<DetectionResult> detect(DetectionRequest request) {
                return result;
            }
        };
    }
}
