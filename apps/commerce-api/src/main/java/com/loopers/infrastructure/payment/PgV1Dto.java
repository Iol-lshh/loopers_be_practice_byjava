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
            // cardNo는 xxxx-xxxx-xxxx-xxxx 형태로 입력받아야 함
            public Transaction {
                if (!cardNo.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}")) {
                    throw new IllegalArgumentException("카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
                }
            }
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
