package com.loopers.application.payment;

import com.loopers.domain.order.OrderCommand;

public class PaymentCriteria {
    public record Pay(
            Long userId,
            Long orderId,
            String paymentType
    ) {
        public OrderCommand.Pay toCommand(Long totalPrice) {
            return new OrderCommand.Pay(
                    userId,
                    orderId,
                    totalPrice,
                    paymentType
            );
        }
    }
}
