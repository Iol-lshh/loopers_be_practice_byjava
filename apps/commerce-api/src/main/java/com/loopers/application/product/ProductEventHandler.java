package com.loopers.application.product;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ProductEventHandler {

    private final ProductService productService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderEvent.Completed event) {
        productService.deduct(event.itemQuantityMap());
    }
}
