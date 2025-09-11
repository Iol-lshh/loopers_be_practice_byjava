package com.loopers.interfaces.consumer.like;

import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.domain.productmetric.ProductMetricService;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.LikeV1Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeV1GlobalEventHandler {
    private final AuditLogService auditLogService;
    ProductMetricService productMetricService;

    @KafkaListener(
            topics = LikeV1Event.TOPIC.UPDATED,
            groupId = "commerce-collector",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handle(
            List<GlobalEvent<LikeV1Event.Updated>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages", messages.size());
        for (var message : messages) {
            auditLogService.log(message.eventId(), LikeV1Event.TOPIC.UPDATED, message.payload());
            ProductMetricCommand.UpdateLikeCount command = new ProductMetricCommand.UpdateLikeCount(
                    message.payload().productId(),
                    message.payload().count()
            );
            productMetricService.updateLikeCount(command);
        }
        acknowledgment.acknowledge();
    }


}
