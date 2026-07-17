package com.pabloncf.threatlens.pipeline;

import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.detection.SecurityEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Persists every {@link SecurityEventMessage} from the security events topic as event
 * history. Decoupled from the request path - detection latency never depends on this.
 */
@Component
public class SecurityEventConsumer {

    private final SecurityEventRepository repository;

    public SecurityEventConsumer(SecurityEventRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${threatlens.kafka.security-events-topic}")
    public void consume(SecurityEventMessage message) {
        repository.save(new SecurityEvent(
                message.eventType(),
                message.sourceIp(),
                message.requestUri(),
                message.httpMethod(),
                message.score(),
                message.severity(),
                message.reason(),
                message.detectedAt()));
    }
}
