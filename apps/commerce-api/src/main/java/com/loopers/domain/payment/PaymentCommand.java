package com.loopers.domain.payment;

import java.util.Map;

public class PaymentCommand {
    public record Pay(
            Long userId,
            Long orderId,
            Long totalPrice
            ) {
        public PaymentEntity toEntity() {
            return PaymentEntity.of(
                    orderId,
                    userId,
                    totalPrice
            );
        }
    }
}
