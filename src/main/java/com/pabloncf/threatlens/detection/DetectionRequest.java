package com.pabloncf.threatlens.detection;

/**
 * The normalized, request-scoped surface a {@link SecurityDetector} inspects.
 *
 * @param payload text content to scan (query string + body); how a real HTTP request is
 *     reduced to this string is left to whatever wires {@link DetectionEngine} into the
 *     request path.
 */
public record DetectionRequest(String sourceIp, String requestUri, String httpMethod, String payload) {
}
