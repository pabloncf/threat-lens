package com.pabloncf.threatlens.detection.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import com.pabloncf.threatlens.detection.SecurityEventType;
import com.pabloncf.threatlens.detection.ratelimit.RateLimiter;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BruteForceDetectorTest {

    private static final BruteForceProperties PROPERTIES =
            new BruteForceProperties(List.of("/login"), 5, Duration.ofMinutes(1));

    @Test
    void ignoresRequestsToNonSensitivePaths() {
        // Arrange
        BruteForceDetector detector = new BruteForceDetector(fixedCountRateLimiter(100), PROPERTIES);
        DetectionRequest request = requestTo("/search");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void doesNotFlagWhenCountIsAtOrBelowThreshold() {
        // Arrange
        BruteForceDetector detector = new BruteForceDetector(fixedCountRateLimiter(5), PROPERTIES);
        DetectionRequest request = requestTo("/login");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void flagsWhenCountExceedsThreshold() {
        // Arrange
        BruteForceDetector detector = new BruteForceDetector(fixedCountRateLimiter(8), PROPERTIES);
        DetectionRequest request = requestTo("/login");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().eventType()).isEqualTo(SecurityEventType.BRUTE_FORCE);
        assertThat(result.get().score()).isEqualTo(80);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
        assertThat(result.get().reason()).contains("8 requests").contains("/login");
    }

    @Test
    void capsScoreAtOneHundred() {
        // Arrange
        BruteForceDetector detector = new BruteForceDetector(fixedCountRateLimiter(1000), PROPERTIES);
        DetectionRequest request = requestTo("/login");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().score()).isEqualTo(100);
        assertThat(result.get().severity()).isEqualTo(Severity.CRITICAL);
    }

    private static DetectionRequest requestTo(String uri) {
        return new DetectionRequest("203.0.113.5", uri, "POST", "username=admin&password=x");
    }

    private static RateLimiter fixedCountRateLimiter(int count) {
        return (key, window) -> count;
    }
}
