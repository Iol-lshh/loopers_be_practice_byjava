package com.loopers.domain.order;

import com.loopers.events.commerce.OrderV1Event;

public interface OrderGlobalEventPublisher {
    void publish(OrderV1Event.Completed event);
}
