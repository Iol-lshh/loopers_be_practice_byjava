package com.loopers.infrastructure.auditlog;

import com.loopers.domain.auditlog.AuditLogEntity;
import org.springframework.data.repository.CrudRepository;

public interface AuditLogJpaRepository extends CrudRepository<AuditLogEntity, Long> {
}
