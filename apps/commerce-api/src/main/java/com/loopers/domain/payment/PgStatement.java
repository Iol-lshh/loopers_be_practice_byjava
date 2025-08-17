package com.loopers.domain.payment;

public class PgStatement {

    public record Request(
            Long orderId,
            Long totalPrice,
            String cardNumber,
            String cardType
    ) {

    }
}
