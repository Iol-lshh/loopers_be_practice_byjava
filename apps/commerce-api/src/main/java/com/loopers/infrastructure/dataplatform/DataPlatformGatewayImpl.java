package com.loopers.infrastructure.dataplatform;

import com.loopers.domain.dataplatform.DataPlatformGateway;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.payment.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class DataPlatformGatewayImpl implements DataPlatformGateway {
    @Override
    public void post(PaymentEvent.Success event) {

    }

    @Override
    public void post(OrderEvent.Completed event) {

    }
}
