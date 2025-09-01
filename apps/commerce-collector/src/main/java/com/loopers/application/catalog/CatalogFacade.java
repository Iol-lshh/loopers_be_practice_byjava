package com.loopers.application.catalog;

import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.domain.catalog.CatalogService;
import com.loopers.events.commerce.ProductV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CatalogFacade {
    private final CatalogService catalogService;
    private final AuditLogService auditLogService;

    public void handle(CatalogCriteria.OutOfStock criteria){
        auditLogService.log(criteria.eventId(), ProductV1Event.TOPIC.OUT_OF_STOCK, criteria.messages());
        catalogService.consumeOutOfStock(criteria.productId());
    }
}
