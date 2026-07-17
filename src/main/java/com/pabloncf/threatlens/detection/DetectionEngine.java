package com.pabloncf.threatlens.detection;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Runs every registered {@link SecurityDetector} against a request and collects whichever
 * results come back. Persisting the results as {@code SecurityEvent}s and publishing them to
 * Kafka happens downstream, in the event pipeline.
 */
@Component
public class DetectionEngine {

    private final List<SecurityDetector> detectors;

    public DetectionEngine(List<SecurityDetector> detectors) {
        this.detectors = detectors;
    }

    public List<DetectionResult> detect(DetectionRequest request) {
        return detectors.stream()
                .map(detector -> detector.detect(request))
                .flatMap(Optional::stream)
                .toList();
    }
}
