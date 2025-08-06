package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentCommand;

import java.util.Map;

public class PaymentCriteria {
    public record Pay(
            Long userId,
            Long orderId,
            String paymentType
    ) {
        public PaymentCommand.Pay toCommand(Long totalPrice, Map<Long, Long> couponMap) {
            return new com.loopers.domain.payment.PaymentCommand.Pay(
                    userId,
                    orderId,
                    totalPrice,
                    paymentType,
                    couponMap
            );
        }
    }
}
