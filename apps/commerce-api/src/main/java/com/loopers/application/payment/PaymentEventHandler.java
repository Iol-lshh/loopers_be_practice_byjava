package com.loopers.application.payment;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.*;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final OrderPaymentSelector orderPaymentSelector;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderEvent.Registered event) {
        log.info("OrderEventHandler.handle 시작 - userId: {}, orderId: {}, totalPrice: {}, paymentType: {}",
                event.userId(), event.orderId(), event.totalPrice(), event.paymentType());
        try {
            OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(event.paymentType());
            log.info("결제 방식 선택: {}", paymentType.name());
            OrderPaymentWay paymentWay = orderPaymentSelector.get(paymentType);

            var paymentCommand = new PaymentCommand.RegisterOrder(event.userId(), event.orderId(), event.totalPrice());
            paymentService.register(paymentCommand);
            paymentWay.request(event.userId(), event.orderId(), event.totalPrice());
        } catch (CoreException e) {
            log.error("결제 요청 처리 실패", e);
            PaymentCommand.Fail failCommand = new PaymentCommand.Fail(event.orderId());
            paymentService.updateState(failCommand);
            throw e;
        }
    }

    @Async
    @EventListener
    public void handle(PaymentEvent.Pg.Pending event) {
        try{
            log.info("PaymentEventHandler.handle 시작 - paymentId: {}, transactionKey: {}, status: {}",
                    event.paymentId(), event.transactionInfo().transactionKey(), "PENDING");
            PaymentCommand.UpdateTransaction command = PaymentCommand.UpdateTransaction
                    .from(event.transactionInfo(), event.userId(), event.paymentId());
            paymentService.update(command);
        } catch (CoreException e) {
            log.error("결제 진행중 상태 업데이트 실패", e);
            PaymentCommand.Fail failCommand = new PaymentCommand.Fail(event.orderId());
            paymentService.updateState(failCommand);
            throw e;
        }
    }
}
