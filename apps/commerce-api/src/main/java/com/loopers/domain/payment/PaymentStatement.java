package com.loopers.domain.payment;

public class PaymentStatement {

    public record Request(
            String orderKey,
            Long userId,
            Long totalPrice,
            String cardNumber,
            String cardType
    ) {

    }
}
