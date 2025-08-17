package com.loopers.domain.payment;

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

    public record Request(
            Long userId,
            Long orderId,
            Long totalPrice
    ){
    }
}
