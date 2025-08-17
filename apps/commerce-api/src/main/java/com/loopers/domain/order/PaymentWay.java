package com.loopers.domain.order;

public interface PaymentWay<R> {
    R request(Long userId, Long orderId, Long totalPrice);
    R pay(Long userId, Long orderId, Long totalPrice);
    OrderEntity.PaymentType getType();
}
