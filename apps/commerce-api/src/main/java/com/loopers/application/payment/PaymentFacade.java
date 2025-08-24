package com.loopers.application.payment;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
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
    private final OrderService orderService;
    private final PointService pointService;
    private final PaymentService paymentService;

    @Transactional
    public PaymentResult.Summary pay(PaymentCriteria.Point criteria) {
        userService.find(criteria.userId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + criteria.userId()));
        OrderEntity order = orderService.find(criteria.orderId()).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다: " + criteria.orderId()));

        OrderCommand.Complete updateCommand = criteria.toCommand(order.getTotalPrice());
        OrderEntity orderEntity = orderService.complete(updateCommand);

        pointService.pay(criteria.userId(), orderEntity.getTotalPrice());

        return PaymentResult.Summary.from(orderEntity);
    }

    public PaymentResult.Summary pay(PaymentCriteria.Transaction criteria) {
        PaymentEntity payment = paymentService.findByOrderKey(criteria.orderKey()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다: " + criteria.orderKey()));
        userService.find(payment.getUserId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + payment.getUserId()));

        PaymentCommand.Transact transactCommand = PaymentCommand.Transact.of(criteria, payment);
        paymentService.transact(transactCommand);

        OrderCommand.Complete updateCommand = criteria.toCommand(payment.getOrderId());
        OrderEntity orderEntity = orderService.complete(updateCommand);
        return PaymentResult.Summary.from(orderEntity);
    }
}
