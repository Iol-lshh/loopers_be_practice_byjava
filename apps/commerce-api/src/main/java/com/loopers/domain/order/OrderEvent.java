package com.loopers.domain.order;

import java.util.List;
import java.util.Map;

public class OrderEvent {
    public record Registered(
        Long userId,
        Long orderId,
        Long totalPrice,
        String paymentType
    ) {
    }

    public record Completed(
        Long orderId,
        Long userId,
        Long totalPrice,
        String paymentType,
        List<Long> couponIds,
        Map<Long, Long> itemQuantityMap
    ) {
        public static Completed from(OrderEntity order) {
            return new Completed(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getPaymentType().getValue(),
                order.getCouponIds(),
                order.getItemQuantityMap()
            );
        }
    }

    public record Canceled(
        Long orderId,
        Long userId,
        Long totalPrice,
        String paymentType
    ){

        public static Canceled from(OrderEntity order) {
            return new Canceled(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getPaymentType().getValue()
            );
        }
    }
}
