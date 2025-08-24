package com.loopers.domain.payment;

public class PaymentEvent {
    public record Failed(
        Long orderId
    ) {
    }
    public record Pending(
        PaymentInfo.Transaction transactionInfo,
        Long userId,
        Long orderId,
        Long paymentId
    ){
    }

    public record Success(
        Long userId,
        Long orderId,
        Long amount
    ) {
        public static Success from(PaymentEntity command) {
            return new Success(
                command.getUserId(),
                command.getOrderId(),
                command.getAmount()
            );
        }
    }
}
