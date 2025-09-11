package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductGlobalEventPublisher;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.ProductV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductGlobalEventPublisherImpl implements ProductGlobalEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Override
    public void publish(ProductV1Event.OutOfStock event) {
        var message = GlobalEvent.of(ProductV1Event.TOPIC.OUT_OF_STOCK, event);
        kafkaTemplate.send(ProductV1Event.TOPIC.OUT_OF_STOCK, event.productId().toString(), message);
    }
}
