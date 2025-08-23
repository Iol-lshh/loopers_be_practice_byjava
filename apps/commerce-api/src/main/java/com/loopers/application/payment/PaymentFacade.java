package com.loopers.application.payment;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentFacade {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final PointService pointService;
    private final PaymentService paymentService;

    @Transactional
    public PaymentResult.Summary pay(PaymentCriteria.Point criteria) {
        userService.find(criteria.userId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + criteria.userId()));

        OrderEntity order = orderService.find(criteria.orderId()).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다: " + criteria.orderId()));

        productService.deduct(order.getItemQuantityMap());
        couponService.useCoupons(criteria.userId(), order.getCouponIds());

        OrderCommand.Complete command = criteria.toCommand(order.getTotalPrice());
        OrderEntity orderEntity = orderService.complete(command);
        pointService.pay(criteria.userId(), orderEntity.getTotalPrice());

        return PaymentResult.Summary.from(orderEntity);
    }

    public PaymentResult.Summary pay(PaymentCriteria.Transaction criteria) {
        PaymentEntity payment = paymentService.findByOrderKey(criteria.orderKey()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다: " + criteria.orderKey()));
        userService.find(payment.getUserId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + payment.getUserId()));

        PaymentCommand.Transaction paymentCommand = PaymentCommand.Transaction.of(criteria, payment);
        paymentService.pay(paymentCommand);

        OrderCommand.Complete command = criteria.toCommand(payment.getUserId(), payment.getOrderId(), payment.getAmount());
        OrderEntity orderEntity = orderService.complete(command);
        productService.deduct(orderEntity.getItemQuantityMap());
        couponService.useCoupons(orderEntity.getUserId(), orderEntity.getCouponIds());

        return PaymentResult.Summary.from(orderEntity);
    }
}
