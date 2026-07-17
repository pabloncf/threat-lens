package com.pabloncf.threatlens.pipeline;

import com.pabloncf.threatlens.detection.DetectionEngine;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs every request through {@link DetectionEngine} and publishes whatever fires. Detection
 * is best-effort: any failure here is logged and swallowed, never allowed to block or fail
 * the request it's inspecting.
 *
 * <p>The inspected payload covers the query string and form parameters (via {@code
 * HttpServletRequest.getParameterMap()}); it does not cover raw JSON request bodies - reading
 * those would require a request wrapper to make the body re-readable for the actual
 * controller, which is left for later.
 */
@Component
public class DetectionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DetectionFilter.class);

    private final DetectionEngine detectionEngine;
    private final SecurityEventProducer producer;

    public DetectionFilter(DetectionEngine detectionEngine, SecurityEventProducer producer) {
        this.detectionEngine = detectionEngine;
        this.producer = producer;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            DetectionRequest detectionRequest = toDetectionRequest(request);
            Instant detectedAt = Instant.now();
            for (DetectionResult result : detectionEngine.detect(detectionRequest)) {
                producer.publish(new SecurityEventMessage(
                        detectionRequest.sourceIp(),
                        detectionRequest.requestUri(),
                        detectionRequest.httpMethod(),
                        result.eventType(),
                        result.score(),
                        result.severity(),
                        result.reason(),
                        detectedAt));
            }
        } catch (RuntimeException e) {
            log.warn("Detection failed for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private DetectionRequest toDetectionRequest(HttpServletRequest request) {
        StringBuilder payload = new StringBuilder();
        request.getParameterMap().forEach((name, values) -> {
            for (String value : values) {
                payload.append(name).append('=').append(value).append('&');
            }
        });
        return new DetectionRequest(
                request.getRemoteAddr(), request.getRequestURI(), request.getMethod(), payload.toString());
    }
}
