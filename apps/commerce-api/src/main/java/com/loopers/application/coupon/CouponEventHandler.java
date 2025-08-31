package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CouponEventHandler {

    private final CouponService couponService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderEvent.Completed event) {
        couponService.useCoupons(event.userId(), event.couponIds());
    }
}
