package com.pabloncf.threatlens.detection;

import java.util.Optional;

/**
 * Strategy interface for a single attack-category detector. New categories (path traversal,
 * SSRF, ...) plug in by implementing this interface; {@link DetectionEngine} never changes.
 */
public interface SecurityDetector {

    SecurityEventType supportedType();

    Optional<DetectionResult> detect(DetectionRequest request);
}
