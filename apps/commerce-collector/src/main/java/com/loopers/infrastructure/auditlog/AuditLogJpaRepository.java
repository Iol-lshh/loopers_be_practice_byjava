package com.loopers.infrastructure.auditlog;

import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.loopers.domain.auditlog.AuditLogEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuditLogJpaRepository extends CrudRepository<AuditLogEntity, Long> {
    Optional<AuditLogEntity> findByEventId(String eventId);
}
