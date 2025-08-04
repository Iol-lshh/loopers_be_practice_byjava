package com.loopers.domain.payment;

public class PaymentCommand {
    public record Pay(
            Long userId,
            Long orderId,
            Long totalPrice,
            String paymentType
            ) {
        public PaymentEntity toEntity() {
            return PaymentEntity.of(
                    orderId,
                    userId,
                    totalPrice,
                    paymentType
            );
        }
    }
}
