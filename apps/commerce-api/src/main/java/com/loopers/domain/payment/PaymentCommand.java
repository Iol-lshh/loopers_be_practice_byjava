package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.domain.order.OrderEntity;

public class PaymentCommand {
    public record Transaction(
            Long userId,
            Long orderId,
            String orderKey,
            String transactionKey,
            String cardType,
            String cardNo,
            String amount,
            String status,
            String reason
            ) {
        public static Transaction of(PaymentCriteria.Transaction criteria, PaymentEntity payment){
            return new Transaction(
                    payment.getUserId(),
                    payment.getId(),
                    criteria.orderKey(),
                    criteria.transactionKey(),
                    criteria.cardType(),
                    criteria.cardNo(),
                    criteria.amount(),
                    criteria.status(),
                    criteria.reason()
            );

        }
    }

    public record Request(
            Long userId,
            Long orderId,
            Long totalPrice
    ){
    }

    public record RegisterOrder(
            Long userId,
            Long orderId,
            Long totalPrice
    ){

    }

    public record UpdateTransaction(
            Long userId,
            Long paymentId,
            String transactionKey,
            String status,
            String reason
    ){
        public static UpdateTransaction from(PaymentInfo.Transaction transaction, Long userId, Long paymentId) {
            return new UpdateTransaction(
                    userId,
                    paymentId,
                    transaction.transactionKey(),
                    transaction.status(),
                    transaction.reason()
            );
        }
    }

    public record Cancel(
        Long orderId
    ){
    }
}
