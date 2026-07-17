package com.pabloncf.threatlens.report;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, UUID> {

    Optional<IncidentReport> findBySecurityEventId(UUID securityEventId);

    @Query(
            value = "SELECT r FROM IncidentReport r JOIN FETCH r.securityEvent",
            countQuery = "SELECT COUNT(r) FROM IncidentReport r")
    Page<IncidentReport> findAllWithSecurityEvent(Pageable pageable);
}
