package com.loopers.application.payment;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.payment.PaymentInfo;

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

    public record Request(
            Long userId,
            Long orderId,
            Long totalPrice,
            String paymentType
    ){
        public static Request from(OrderEvent.Registered event){
            return new Request(
                    event.userId(),
                    event.orderId(),
                    event.totalPrice(),
                    event.paymentType()
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

    public record Update(
            PaymentInfo.Transaction transactionInfo,
            Long userId,
            Long orderId,
            Long paymentId
    ) {
        public static Update from(PaymentEvent.Pg.Pending event) {
            return new Update(
                    event.transactionInfo(),
                    event.userId(),
                    event.orderId(),
                    event.paymentId()
            );
        }
    }
}
