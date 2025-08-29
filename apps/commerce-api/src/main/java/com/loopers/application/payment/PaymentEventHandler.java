package com.loopers.application.payment;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventHandler {

    private final PaymentService paymentService;
    private final PaymentFacade paymentFacade;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderEvent.Registered event) {
        log.info("PaymentEventHandler OrderEvent.Registered 시작 - userId: {}, orderId: {}, totalPrice: {}, paymentType: {}",
                event.userId(), event.orderId(), event.totalPrice(), event.paymentType());

        PaymentCriteria.Request criteria = PaymentCriteria.Request.from(event);
        paymentFacade.request(criteria);
    }

    @Async
    @EventListener
    public void handle(PaymentEvent.Pg.Pending event) {
        log.info("PaymentEventHandler PaymentEvent.Pg.Pending 시작 - paymentId: {}, transactionKey: {}, status: {}",
                event.paymentId(), event.transactionInfo().transactionKey(), "PENDING");
        PaymentCriteria.Update criteria = PaymentCriteria.Update.from(event);
        paymentFacade.update(criteria);
    }

    @Async
    @EventListener
    public void handle(PaymentEvent.Pg.Failed event) {
        log.info("PaymentEventHandler PaymentEvent.Pg.Failed 시작 - orderId: {}",
                event.orderId());
        PaymentCommand.Fail command = new PaymentCommand.Fail(event.orderId());
        paymentService.updateState(command);
    }
}
