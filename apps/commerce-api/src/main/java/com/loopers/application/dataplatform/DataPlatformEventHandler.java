package com.loopers.application.dataplatform;

import com.loopers.domain.dataplatform.DataPlatformGateway;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class DataPlatformEventHandler {
    private final DataPlatformGateway dataPlatformGateway;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentEvent.Success event) {
        dataPlatformGateway.post(event);
    }

    @TransactionalEventListener(phase =  TransactionPhase.AFTER_COMMIT)
    public void handle(OrderEvent.Completed event) {
        dataPlatformGateway.post(event);
    }
}
