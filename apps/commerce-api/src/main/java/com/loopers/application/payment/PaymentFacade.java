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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {
    private final UserService userService;
    private final OrderService orderService;
    private final PointService pointService;
    private final PaymentService paymentService;
    private final OrderPaymentSelector orderPaymentSelector;

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

    public void request(PaymentCriteria.Request criteria) {
        try {
            OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(criteria.paymentType());
            log.info("결제 방식 선택: {}", paymentType.name());

            var paymentCommand = new PaymentCommand.RegisterOrder(criteria.userId(), criteria.orderId(), criteria.totalPrice());
            paymentService.register(paymentCommand);

            OrderPaymentWay paymentWay = orderPaymentSelector.get(paymentType);
            paymentWay.request(criteria.userId(), criteria.orderId(), criteria.totalPrice());
        } catch (CoreException e) {
            log.error("결제 요청 처리 실패", e);
            PaymentCommand.Fail failCommand = new PaymentCommand.Fail(criteria.orderId());
            paymentService.updateState(failCommand);
            throw e;
        }
    }

    public void update(PaymentCriteria.Update criteria) {
        try{
            PaymentCommand.UpdateTransaction command = PaymentCommand.UpdateTransaction
                    .from(criteria.transactionInfo(), criteria.userId(), criteria.paymentId());
            paymentService.update(command);
        } catch (CoreException e) {
            log.error("결제 진행중 상태 업데이트 실패", e);
            PaymentCommand.Fail failCommand = new PaymentCommand.Fail(criteria.orderId());
            paymentService.updateState(failCommand);
            throw e;
        }
    }
}
