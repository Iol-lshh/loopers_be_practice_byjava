package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;

public class PaymentResult {
    public record Summary(
            Long paymentId,
            Long userId,
            Long orderId,
            String paymentType,
            Long totalAmount
    ){
        public static Summary from(PaymentEntity payment) {
            return new Summary(
                    payment.getId(),
                    payment.getUserId(),
                    payment.getOrderId(),
                    payment.getType().name(),
                    payment.getAmount()
            );
        }
    }

    public record Discount(
            Long id,
            String type,
            Long amount
    ){
    }
}
