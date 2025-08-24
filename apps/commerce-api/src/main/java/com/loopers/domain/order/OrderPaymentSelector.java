package com.loopers.domain.order;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPaymentSelector {
    private final Map<OrderEntity.PaymentType, OrderPaymentWay> paymentMethods;

    public OrderPaymentSelector(List<OrderPaymentWay> orderPaymentWays) {
        this.paymentMethods = orderPaymentWays.stream()
                .collect(Collectors.toMap(OrderPaymentWay::getType, paymentWay -> paymentWay));
    }

    public OrderPaymentWay get(OrderEntity.PaymentType paymentType) {
        return paymentMethods.get(paymentType);
    }
}
