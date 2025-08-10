package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentWay;
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
        public PointEntity pay(Long userId, Long totalPrice) {
            return pointService.pay(userId, totalPrice);
        }

        @Override
        public Type getType() {
            return Type.POINT;
        }
    }
}
