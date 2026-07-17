package com.pabloncf.threatlens.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.DetectionEngine;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import com.pabloncf.threatlens.detection.SecurityDetector;
import com.pabloncf.threatlens.detection.SecurityEventType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class DetectionFilterTest {

    @Test
    void publishesASecurityEventMessageForEachDetectionResult() throws Exception {
        // Arrange
        DetectionEngine engine = new DetectionEngine(List.of(fakeDetector(
                new DetectionResult(SecurityEventType.SQL_INJECTION, 80, Severity.HIGH, "tautology"))));
        SecurityEventProducer producer = mock(SecurityEventProducer.class);
        DetectionFilter filter = new DetectionFilter(engine, producer);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/search");
        request.setRemoteAddr("203.0.113.5");
        request.setParameter("q", "' OR '1'='1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        ArgumentCaptor<SecurityEventMessage> captor = ArgumentCaptor.forClass(SecurityEventMessage.class);
        verify(producer).publish(captor.capture());
        SecurityEventMessage message = captor.getValue();
        assertThat(message.sourceIp()).isEqualTo("203.0.113.5");
        assertThat(message.requestUri()).isEqualTo("/search");
        assertThat(message.httpMethod()).isEqualTo("GET");
        assertThat(message.eventType()).isEqualTo(SecurityEventType.SQL_INJECTION);
        assertThat(message.score()).isEqualTo(80);
        assertThat(message.severity()).isEqualTo(Severity.HIGH);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void alwaysContinuesTheFilterChainWhenNothingFires() throws Exception {
        // Arrange
        DetectionEngine engine = new DetectionEngine(List.of(fakeDetector(null)));
        SecurityEventProducer producer = mock(SecurityEventProducer.class);
        DetectionFilter filter = new DetectionFilter(engine, producer);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/comments");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(producer, never()).publish(any());
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void neverBlocksTheRequestWhenDetectionThrows() throws Exception {
        // Arrange
        SecurityDetector throwing = new SecurityDetector() {
            @Override
            public SecurityEventType supportedType() {
                return SecurityEventType.XSS;
            }

            @Override
            public Optional<DetectionResult> detect(DetectionRequest request) {
                throw new IllegalStateException("boom");
            }
        };
        DetectionEngine engine = new DetectionEngine(List.of(throwing));
        SecurityEventProducer producer = mock(SecurityEventProducer.class);
        DetectionFilter filter = new DetectionFilter(engine, producer);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/comments");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(chain.getRequest()).isNotNull();
        verify(producer, times(0)).publish(any());
    }

    private static SecurityDetector fakeDetector(DetectionResult result) {
        return new SecurityDetector() {
            @Override
            public SecurityEventType supportedType() {
                return SecurityEventType.SQL_INJECTION;
            }

            @Override
            public Optional<DetectionResult> detect(DetectionRequest request) {
                return Optional.ofNullable(result);
            }
        };
    }
}
