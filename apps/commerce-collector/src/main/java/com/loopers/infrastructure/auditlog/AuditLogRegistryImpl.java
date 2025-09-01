package com.loopers.infrastructure.auditlog;

import com.loopers.domain.auditlog.AuditLogEntity;
import com.loopers.domain.auditlog.AuditLogRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuditLogRegistryImpl implements AuditLogRegistry {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public AuditLogEntity save(AuditLogEntity auditLogEntity) {
        return auditLogJpaRepository.save(auditLogEntity);
    }
}
