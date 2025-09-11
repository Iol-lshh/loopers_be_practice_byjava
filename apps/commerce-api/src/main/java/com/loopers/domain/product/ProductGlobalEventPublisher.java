package com.loopers.domain.product;

import com.loopers.events.commerce.ProductV1Event;

public interface ProductGlobalEventPublisher {
    void publish(ProductV1Event.OutOfStock event);
}
