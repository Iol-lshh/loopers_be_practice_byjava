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
                    orderId
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
        public OrderCommand.Complete toCommand(Long orderId) {
            return new OrderCommand.Complete(
                    orderId
            );
        }
    }
}
