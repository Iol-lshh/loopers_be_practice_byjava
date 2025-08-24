package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;

public class PaymentV1Dto {
    public static class Request {
        public record Pay(
                Long userId,
                Long orderId,
                String paymentType
        ) {
        }

        public record Transaction(
                String transactionKey,
                String orderId,
                String cardType,
                String cardNo,
                String amount,
                String status,
                String reason
        ) {
        }
    }

    public static class Response {
        public record Summary(
                Long userId,
                Long orderId,
                String paymentType,
                Long totalAmount
        ) {
            public static Summary from(PaymentResult.Summary payment) {
                return new Summary(
                        payment.userId(),
                        payment.orderId(),
                        payment.paymentType(),
                        payment.totalAmount()
                );
            }
        }
    }
}
