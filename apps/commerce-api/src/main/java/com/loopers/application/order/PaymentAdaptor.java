package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderPaymentWay;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class PaymentAdaptor {
    @RequiredArgsConstructor
    @Component
    public static class PointAdaptor implements OrderPaymentWay {

        private final PointService pointService;

        @Override
        public void request(Long userId, Long orderId, Long totalPrice) {
            // do nothing
        }

        @Override
        public OrderEntity.PaymentType getType() {
            return OrderEntity.PaymentType.POINT;
        }
    }

    @RequiredArgsConstructor
    @Component
    public static class PGAdaptor implements OrderPaymentWay {

        private final PaymentService paymentService;

        @Override
        public void request(Long userId, Long orderId, Long totalPrice) {
            PaymentCommand.Request requestCommand = new PaymentCommand.Request(
                    userId,
                    orderId,
                    totalPrice
            );
            paymentService.request(requestCommand);
        }

        @Override
        public OrderEntity.PaymentType getType() {
            return OrderEntity.PaymentType.PG;
        }
    }
}
