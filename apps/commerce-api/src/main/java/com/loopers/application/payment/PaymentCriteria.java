package com.loopers.application.payment;

import com.loopers.domain.order.OrderCommand;

public class PaymentCriteria {
    public record Point(
            Long userId,
            Long orderId,
            String paymentType
    ) {
        public OrderCommand.Complete toCommand(Long totalPrice) {
            return new OrderCommand.Complete(
                    userId,
                    orderId,
                    totalPrice,
                    "POINT"
            );
        }
    }

    public record Transaction(
            String transactionKey,
            String orderKey,
            String cardType,
            String cardNo,
            String amount,
            String status,
            String reason
    ){
        public OrderCommand.Complete toCommand(Long userId, Long orderId, Long totalPrice) {
            return new OrderCommand.Complete(
                    userId,
                    orderId,
                    totalPrice,
                    "PG"
            );
        }
    }
}
