package com.loopers.application.payment;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderInfo;

public class PaymentResult {
    public record Summary(
            Long userId,
            Long orderId,
            Long totalAmount,
            String paymentType,
            String state
    ){
        public static Summary from(OrderEntity order) {
            return new Summary(
                    order.getUserId(),
                    order.getId(),
                    order.getTotalPrice(),
                    order.getPaymentType().getValue(),
                    order.getState().getValue()
            );
        }
    }
}
