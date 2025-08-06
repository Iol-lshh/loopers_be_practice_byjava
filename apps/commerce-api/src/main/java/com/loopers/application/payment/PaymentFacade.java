package com.loopers.application.payment;

import com.loopers.domain.payment.*;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class PaymentFacade {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public PaymentResult.Summary pay(PaymentCriteria.Pay criteria) {
        userService.find(criteria.userId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + criteria.userId()));

        OrderEntity order = orderService.find(criteria.orderId()).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다: " + criteria.orderId()));

        Map<Long, Long> itemQuantityMap = order.getItemQuantityMap();
        productService.deduct(itemQuantityMap);

        orderService.complete(order.getId());

        PaymentCommand.Pay command = criteria.toCommand(order.getTotalPrice(), order.getAppliedCouponValueMap());
        PaymentEntity payment = paymentService.pay(command);
        return PaymentResult.Summary.from(payment);
    }
}
