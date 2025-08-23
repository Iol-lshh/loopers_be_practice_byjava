package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventHandler {
    private final OrderPaymentSelector orderPaymentSelector;
    private final OrderService orderService;
    private final OrderFacade orderFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCommand.RequestPayment command) {
        log.info("OrderEventHandler.handle 시작 - userId: {}, orderId: {}, totalPrice: {}, paymentType: {}",
                command.userId(), command.orderId(), command.totalPrice(), command.paymentType());
        
        try {
            OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(command.paymentType());
            log.info("결제 타입 변환 성공: {}", paymentType);
            
            OrderPaymentWay paymentWay = orderPaymentSelector.get(paymentType);
            log.info("결제 방식 선택: {}", paymentWay.getClass().getSimpleName());
            
            paymentWay.request(
                command.userId(),
                command.orderId(),
                command.totalPrice()
            );
            log.info("결제 요청 완료");
        } catch (Exception e) {
            OrderCommand.Cancel cancelCommand = new OrderCommand.Cancel(command.orderId());
            orderService.cancel(cancelCommand);
            log.error("결제 요청 처리 실패", e);
            throw e;
        }
    }

    @EventListener
    public void handle(PaymentCommand.Cancel command) {
        log.info("OrderEventHandler.handle 주문 취소 시작 - orderId: {}",
                command.orderId());
        OrderCommand.Cancel orderCommand = new OrderCommand.Cancel(command.orderId());
        orderService.cancel(orderCommand);
    }

    @EventListener
    public void handle(OrderCommand.Complete command) {
        log.info("OrderEventHandler.handle 주문 완료 시작 - orderId: {}",
                command.orderId());
        orderFacade.complete(command);
    }
}
