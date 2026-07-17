package com.pabloncf.threatlens.triage;

import com.pabloncf.threatlens.common.Severity;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable AI triage behavior. See {@code application.yml} for defaults.
 *
 * @param minSeverity only events at or above this severity are sent to Claude - the cost gate
 *     that keeps benign/low-signal traffic out of the LLM path.
 */
@ConfigurationProperties(prefix = "threatlens.triage")
public record TriageProperties(String model, Severity minSeverity) {
}
