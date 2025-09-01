package com.loopers.domain.auditlog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuditLogService {

    private final AuditLogRegistry auditLogRegistry;

    public void log(String eventId, String topic, Object messages) {
        AuditLogEntity auditLogEntity = new AuditLogEntity(eventId, topic, messages);
        auditLogRegistry.save(auditLogEntity);
    }
}
