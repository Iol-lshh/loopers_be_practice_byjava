package com.loopers.domain.order;

public class OrderInfo {

    public record PaymentInfo(
            Long orderId,
            Long userId,
            Long amount,
            OrderEntity.PaymentType type,
            OrderEntity.State state
    ){
    }
}
