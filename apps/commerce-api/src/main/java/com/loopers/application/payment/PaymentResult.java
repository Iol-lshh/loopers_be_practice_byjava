package com.loopers.application.payment;

import com.loopers.domain.order.OrderInfo;

public class PaymentResult {
    public record Summary(
            Long userId,
            Long orderId,
            Long totalAmount,
            String paymentType,
            String state
    ){
        public static Summary from(OrderInfo.Pay payment) {
            return new Summary(
                    payment.userId(),
                    payment.orderId(),
                    payment.amount(),
                    payment.type().getValue(),
                    payment.state().getValue()
            );
        }
    }
}
