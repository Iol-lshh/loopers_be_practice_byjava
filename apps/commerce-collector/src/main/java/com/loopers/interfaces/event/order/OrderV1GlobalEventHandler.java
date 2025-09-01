package com.loopers.interfaces.event.order;

import com.loopers.domain.auditlog.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderV1GlobalEventHandler {
    private final AuditLogService auditLogService;


}
