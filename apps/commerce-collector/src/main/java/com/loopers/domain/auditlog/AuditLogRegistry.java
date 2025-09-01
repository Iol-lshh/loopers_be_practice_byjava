package com.loopers.domain.auditlog;


public interface AuditLogRegistry {
    AuditLogEntity save(AuditLogEntity auditLogEntity);
}
