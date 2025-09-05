package com.loopers.domain.auditlog;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AuditLogService {

    private final AuditLogRegistry auditLogRegistry;

    @Transactional
    public void log(String eventId, String topic, Object messages) {
        if(auditLogRegistry.findByEventId(eventId).isPresent()) {
            throw new CoreException(
                    ErrorType.CONFLICT,
                    "이미 존재하는 eventId 입니다 : " + eventId
            );
        }
        AuditLogEntity auditLogEntity = new AuditLogEntity(eventId, topic, messages);
        auditLogRegistry.save(auditLogEntity);
    }
}
