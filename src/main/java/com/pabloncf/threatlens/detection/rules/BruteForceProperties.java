package com.pabloncf.threatlens.detection.rules;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable thresholds for {@link BruteForceDetector}. See {@code application.yml} for
 * defaults.
 */
@ConfigurationProperties(prefix = "threatlens.detection.brute-force")
public record BruteForceProperties(List<String> sensitivePaths, int threshold, Duration window) {
}
