package com.loopers.application.product;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.product.ProductEvent;
import com.loopers.domain.product.ProductGlobalEventPublisher;
import com.loopers.domain.product.ProductService;
import com.loopers.events.commerce.ProductV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ProductEventHandler {

    private final ProductService productService;
    private final ProductGlobalEventPublisher productGlobalEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderEvent.Completed event) {
        productService.deduct(event.itemQuantityMap());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductEvent.Domain.OutOfStock event) {
        var globalEvent = new ProductV1Event.OutOfStock(event.productId());
        productGlobalEventPublisher.publish(globalEvent);
    }
}
