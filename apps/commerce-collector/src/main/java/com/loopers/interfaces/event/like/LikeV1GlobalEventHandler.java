package com.loopers.interfaces.event.like;

import com.loopers.application.productmetric.ProductMetricFacade;
import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.LikeV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeV1GlobalEventHandler {
    private final AuditLogService auditLogService;
    ProductMetricFacade productMetricFacade;

    @KafkaListener(
            topics = LikeV1Event.TOPIC.UPDATED,
            groupId = "commerce-collector"
    )
    public void handle(
            GlobalEvent<LikeV1Event.Updated> messages,
            Acknowledgment acknowledgment
    ) {
        auditLogService.log(messages.eventId(), LikeV1Event.TOPIC.UPDATED, messages.payload());
        var command = new ProductMetricCommand.UpdateLikeCount(
                messages.payload().productId(),
                messages.payload().count()
        );
        productMetricFacade.update(command);
        acknowledgment.acknowledge();
    }
}
