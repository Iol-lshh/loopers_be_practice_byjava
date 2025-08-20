package com.loopers.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class OrderPaymentHandler {
    private final OrderPaymentSelector orderPaymentSelector;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCommand.RequestPayment command) {
        OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(command.paymentType());
        orderPaymentSelector.get(paymentType).request(
            command.userId(),
            command.orderId(),
            command.totalPrice()
        );
    }
}
