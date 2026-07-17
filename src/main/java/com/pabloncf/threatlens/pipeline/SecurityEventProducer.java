package com.pabloncf.threatlens.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link SecurityEventMessage}s to the security events topic. Fire-and-forget:
 * callers (the request-path {@code DetectionFilter}) must never block on delivery.
 */
@Component
public class SecurityEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventProducer.class);

    private final KafkaTemplate<String, SecurityEventMessage> kafkaTemplate;
    private final String topic;

    public SecurityEventProducer(
            KafkaTemplate<String, SecurityEventMessage> kafkaTemplate,
            @Value("${threatlens.kafka.security-events-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(SecurityEventMessage message) {
        kafkaTemplate.send(topic, message.sourceIp(), message)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.warn("Failed to publish security event for {} {}: {}",
                                message.httpMethod(), message.requestUri(), exception.getMessage());
                    }
                });
    }
}
