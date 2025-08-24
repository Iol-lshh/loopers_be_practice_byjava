package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventHandler {
    private final OrderService orderService;

    @Async
    @EventListener
    public void handle(PaymentEvent.Failed event) {
        log.info("OrderEventHandler.handle 주문 취소 시작 - orderId: {}",
                event.orderId());
        OrderCommand.Cancel orderCommand = new OrderCommand.Cancel(event.orderId());
        orderService.cancel(orderCommand);
    }

    @Async
    @EventListener
    public void handle(PaymentEvent.Success event) {
        log.info("OrderEventHandler.handle 주문 완료 시작 - orderId: {}",
                event.orderId());
        OrderCommand.Complete command = new OrderCommand.Complete(event.orderId());
        orderService.complete(command);
    }
}
