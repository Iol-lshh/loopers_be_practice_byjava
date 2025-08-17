package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record Item(
        Long productId,
        Long price,
        Long quantity
    ) {
    }

    public record Coupon(
        Long id,
        Long value
    ) {
    }

    public record Order(
        Long userId,
        String paymentType,
        List<Item> orderItems,
        List<Coupon> orderCoupons
    ) {
    }

    public record Pay(
        Long userId,
        Long orderId,
        Long totalPrice,
        String paymentType
    ){

    }
}
