package com.loopers.interfaces.consumer.catalog;

import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.domain.catalog.CatalogService;
import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.domain.productmetric.ProductMetricService;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.ProductV1Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CatalogV1GlobalEventHandler {
    private final CatalogService catalogService;
    private final AuditLogService auditLogService;
    private final ProductMetricService productMetricService;

    @KafkaListener(
            topics = ProductV1Event.TOPIC.OUT_OF_STOCK,
            groupId = "commerce-collector",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleOutOfStock(
            List<GlobalEvent<ProductV1Event.OutOfStock>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("handle ProductV1Event.OutOfStock messages: {}", messages.size());
        for (var message : messages) {
            auditLogService.log(message.eventId(), ProductV1Event.TOPIC.OUT_OF_STOCK, message.payload());
            catalogService.consumeOutOfStock(message.payload().productId());
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = ProductV1Event.TOPIC.VIEWED,
            groupId = "commerce-collector",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleViewed(
            List<GlobalEvent<ProductV1Event.Viewed>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("handle ProductV1Event.Viewed messages: {}", messages.size());
        for (var message : messages) {
            auditLogService.log(message.eventId(), ProductV1Event.TOPIC.VIEWED, message);
            var payload = message.payload();
            ProductMetricCommand.AddViewCount command = new ProductMetricCommand.AddViewCount(
                    payload.productId(),
                    1L
            );
            productMetricService.addViewCount(command);
        }
        acknowledgment.acknowledge();
    }
}
