package com.pabloncf.threatlens.pipeline;

import com.pabloncf.threatlens.detection.SecurityEvent;
import com.pabloncf.threatlens.detection.SecurityEventRepository;
import com.pabloncf.threatlens.triage.IncidentTriageService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Persists every {@link SecurityEventMessage} from the security events topic as event
 * history, then hands the persisted event to {@link IncidentTriageService}. Both steps happen
 * here, in the same listener, rather than in a second parallel consumer: {@code IncidentReport}
 * has a FK to the persisted {@code SecurityEvent}, which only exists once this save completes.
 * Still fully decoupled from the request path - detection latency never depends on this.
 */
@Component
public class SecurityEventConsumer {

    private final SecurityEventRepository repository;
    private final IncidentTriageService triageService;

    public SecurityEventConsumer(SecurityEventRepository repository, IncidentTriageService triageService) {
        this.repository = repository;
        this.triageService = triageService;
    }

    @KafkaListener(topics = "${threatlens.kafka.security-events-topic}")
    public void consume(SecurityEventMessage message) {
        SecurityEvent event = repository.save(new SecurityEvent(
                message.eventType(),
                message.sourceIp(),
                message.requestUri(),
                message.httpMethod(),
                message.score(),
                message.severity(),
                message.reason(),
                message.detectedAt()));
        triageService.triageIfSuspicious(event);
    }
}
