package com.pabloncf.threatlens.detection;

/**
 * Category of attack a detector recognized in a request.
 */
public enum SecurityEventType {
    SQL_INJECTION,
    XSS,
    BRUTE_FORCE,
    TRAFFIC_ANOMALY
}
