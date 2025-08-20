package com.loopers.domain.order;

public interface OrderPaymentWay {
    void request(Long userId, Long orderId, Long totalPrice);
    OrderEntity.PaymentType getType();
}
