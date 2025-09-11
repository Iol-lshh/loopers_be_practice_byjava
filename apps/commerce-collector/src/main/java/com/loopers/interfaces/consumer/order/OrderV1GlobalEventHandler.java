package com.loopers.interfaces.consumer.order;

import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.domain.productmetric.ProductMetricService;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.OrderV1Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderV1GlobalEventHandler {
    private final AuditLogService auditLogService;
    private final ProductMetricService productMetricService;

    @KafkaListener(
            topics = OrderV1Event.TOPIC.COMPLETED,
            groupId = "commerce-collector",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handle(
            List<GlobalEvent<OrderV1Event.Completed>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("handle OrderV1Event.Completed messages: {}", messages.size());
        for (var message : messages) {
            auditLogService.log(message.eventId(), OrderV1Event.TOPIC.COMPLETED, message.payload());
            List<ProductMetricCommand.AddSoldCount> commands = message.payload().itemQuantityMap()
                    .entrySet()
                    .stream()
                    .map(entry -> new ProductMetricCommand.AddSoldCount(
                            entry.getKey(),
                            entry.getValue()
                    ))
                    .toList();
            productMetricService.addSoldCount(commands);
        }
        acknowledgment.acknowledge();
    }
}
