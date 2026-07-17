package com.pabloncf.threatlens.detection.ratelimit;

import java.time.Duration;

/**
 * Records an occurrence under {@code key} and returns how many occurrences fall within the
 * trailing {@code window}, so callers can decide whether a threshold was crossed.
 */
public interface RateLimiter {

    int recordAndCount(String key, Duration window);
}
