package com.pabloncf.threatlens.detection.rules;

import com.pabloncf.threatlens.common.Severity;
import com.pabloncf.threatlens.detection.DetectionRequest;
import com.pabloncf.threatlens.detection.DetectionResult;
import com.pabloncf.threatlens.detection.SecurityDetector;
import com.pabloncf.threatlens.detection.SecurityEventType;
import com.pabloncf.threatlens.detection.ratelimit.RateLimiter;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Flags repeated requests to a sensitive endpoint (e.g. {@code /login}) from the same source
 * IP within a configurable window. Without a real Filter/interceptor in the request path
 * (added when the event pipeline is wired up), this can't yet tell a failed login from a
 * successful one - "too many requests, same origin, same endpoint" is the heuristic for now.
 */
@Component
public class BruteForceDetector implements SecurityDetector {

    private final RateLimiter rateLimiter;
    private final BruteForceProperties properties;

    public BruteForceDetector(RateLimiter rateLimiter, BruteForceProperties properties) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    public SecurityEventType supportedType() {
        return SecurityEventType.BRUTE_FORCE;
    }

    @Override
    public Optional<DetectionResult> detect(DetectionRequest request) {
        if (properties.sensitivePaths().stream().noneMatch(request.requestUri()::startsWith)) {
            return Optional.empty();
        }

        String key = "bruteforce:" + request.sourceIp() + ":" + request.requestUri();
        int count = rateLimiter.recordAndCount(key, properties.window());
        if (count <= properties.threshold()) {
            return Optional.empty();
        }

        int overBy = count - properties.threshold();
        int score = Math.min(100, 50 + overBy * 10);
        String reason = "%d requests to %s from %s within %s (threshold %d)"
                .formatted(count, request.requestUri(), request.sourceIp(), properties.window(), properties.threshold());
        return Optional.of(new DetectionResult(SecurityEventType.BRUTE_FORCE, score, Severity.fromScore(score), reason));
    }
}
