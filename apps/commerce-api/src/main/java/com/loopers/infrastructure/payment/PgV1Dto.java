package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentInfo;

import java.util.List;

public class PgV1Dto {

    public static class Request {
        public record Transaction(
                String orderId,
                String cardType,
                String cardNo,
                Long amount,
                String callbackUrl
        ) {
        }
    }

    public static class Response {
        public record Transaction(
                String transactionKey,
                String status,
                String reason
        ) {
            public PaymentInfo.Transaction getInfo() {
                return new PaymentInfo.Transaction(transactionKey, status, reason);
            }
        }

        public record Order(
                String orderId,
                List<Transaction> transactions
        ){
            public PaymentInfo.Order getInfo() {
                List<PaymentInfo.Transaction> list = PaymentInfo.Transaction.ofAll(transactions);
                return new PaymentInfo.Order(orderId, list);
            }
        }
    }
}
