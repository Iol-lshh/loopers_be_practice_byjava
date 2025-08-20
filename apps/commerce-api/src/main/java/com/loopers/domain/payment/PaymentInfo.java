package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.PgV1Dto;

import java.util.List;

public class PaymentInfo {
    public record Transaction(
            String transactionKey,
            String status,
            String reason
    ) {
        public static List<Transaction> ofAll(List<PgV1Dto.Response.Transaction> transactions) {
            return transactions.stream()
                    .map(t -> new Transaction(t.transactionKey(), t.status(), t.reason()))
                    .toList();
        }
    }

    public record Order(
            String orderId,
            List<PaymentInfo.Transaction> transactions
    ){
    }

}
