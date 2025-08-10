package com.loopers.domain.payment;

import java.util.Map;

public class PaymentCommand {
    public record Pay(
            Long userId,
            Long orderId,
            Long totalPrice,
            String paymentType,
            Map<Long, Long> couponMap
            ) {
        public PaymentEntity toEntity() {
            return PaymentEntity.of(
                    orderId,
                    userId,
                    totalPrice,
                    paymentType,
                    couponMap
            );
        }
    }
}
