package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.PaymentWay;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class PaymentAdaptor {
    @RequiredArgsConstructor
    @Component
    public static class PointAdaptor implements PaymentWay<PointEntity> {

        private final PointService pointService;

        @Override
        public void request(Long userId, Long orderId, Long totalPrice) {
            // do nothing
        }

        @Override
        public PointEntity pay(Long userId, Long orderId, Long totalPrice) {
            return pointService.pay(userId, totalPrice);
        }

        @Override
        public OrderEntity.PaymentType getType() {
            return OrderEntity.PaymentType.POINT;
        }
    }

    @RequiredArgsConstructor
    @Component
    public static class PGAdaptor implements PaymentWay<PaymentEntity> {

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
        public PaymentEntity pay(Long userId, Long orderId, Long totalPrice) {
            PaymentCommand.Pay paymentCommand = new PaymentCommand.Pay(
                    userId,
                    orderId,
                    totalPrice
            );
            return paymentService.pay(paymentCommand);
        }

        @Override
        public OrderEntity.PaymentType getType() {
            return null;
        }
    }
}
