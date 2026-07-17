package com.pabloncf.threatlens.report;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only API over incident reports. Never exposes JPA entities directly - see
 * {@link IncidentReportResponse}.
 */
@RestController
@RequestMapping("/api/incident-reports")
public class IncidentReportController {

    private final IncidentReportRepository repository;

    public IncidentReportController(IncidentReportRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<IncidentReportResponse> list(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return repository.findAllWithSecurityEvent(pageable).map(IncidentReportResponse::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentReportResponse> get(@PathVariable UUID id) {
        return repository
                .findById(id)
                .map(IncidentReportResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
