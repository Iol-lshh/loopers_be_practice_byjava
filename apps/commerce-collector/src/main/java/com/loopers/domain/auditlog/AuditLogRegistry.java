package com.loopers.domain.auditlog;

import java.util.Optional;

public interface AuditLogRegistry {
    AuditLogEntity save(AuditLogEntity auditLogEntity);

    Optional<AuditLogEntity> findByEventId(String eventId);
}
