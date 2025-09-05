package com.loopers.interfaces.event.catalog;

import com.loopers.application.catalog.CatalogFacade;
import com.loopers.application.catalog.CatalogCriteria;
import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.ProductV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CatalogV1GlobalEventHandler {
    private final CatalogFacade catalogFacade;
    private final AuditLogService auditLogService;

    @KafkaListener(
            topics = ProductV1Event.TOPIC.OUT_OF_STOCK,
            groupId = "commerce-collector"
    )
    public void handle(
            GlobalEvent<ProductV1Event.OutOfStock> messages,
            Acknowledgment acknowledgment
    ) {
        auditLogService.log(messages.eventId(), ProductV1Event.TOPIC.OUT_OF_STOCK, messages.payload());
        CatalogCriteria.OutOfStock command = new CatalogCriteria.OutOfStock(messages.eventId(), messages.payload().productId(), messages);
        catalogFacade.handle(command);
        acknowledgment.acknowledge();
    }
}
