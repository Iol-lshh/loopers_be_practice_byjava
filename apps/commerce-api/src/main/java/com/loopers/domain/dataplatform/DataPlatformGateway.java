package com.loopers.domain.dataplatform;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.payment.PaymentEvent;

public interface DataPlatformGateway {
    void post(PaymentEvent.Success event);

    void post(OrderEvent.Completed event);
}
