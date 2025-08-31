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

    public record Complete(
        Long orderId
    ){
    }

    public record Cancel(
            Long orderId
    ){
    }

    public record Transaction(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        String status,
        String reason
    ) {
    }

}
