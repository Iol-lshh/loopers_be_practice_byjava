package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderGlobalEventPublisher;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.OrderV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderGlobalEventPublisherImpl implements OrderGlobalEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Override
    public void publish(OrderV1Event.Completed event) {
        var message = GlobalEvent.of(OrderV1Event.TOPIC.COMPLETED, event);
        kafkaTemplate.send(
                OrderV1Event.TOPIC.COMPLETED,
                event.orderId().toString(),
                message
        );
    }
}
