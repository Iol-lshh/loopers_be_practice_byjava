package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;

public class OrderResult {
    public record Summary(
        Long orderId,
        Long userId,
        String orderDate,
        long totalPrice
    ) {
        public static Summary from(OrderEntity order) {
            return new Summary(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalPrice()
            );
        }
    }

    public record Detail(
        Long orderId,
        Long userId,
        String orderDate,
        long totalPrice,
        List<Item> items,
        List<Coupon> coupons
    ) {
        public static Detail from(OrderEntity order, List<ProductEntity> products) {
            List<Item> itemInfos = order.getOrderItems().stream()
                .map(item -> {
                    ProductEntity product = products.stream()
                        .filter(p -> p.getId().equals(item.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다: " + item.getProductId()));
                    return new Item(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        item.getPrice()
                    );
                })
                .toList();

            List<Coupon> coupons = order.getOrderCoupons().stream()
                .map(coupon -> new Coupon(coupon.getCouponId(), coupon.getValue()))
                .toList();

            return new Detail(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalPrice(),
                itemInfos,
                coupons
            );
        }
    }

    public record Item(
        Long productId,
        String productName,
        Long quantity,
        Long price
    ) {
    }

    public record Coupon(
        Long couponId,
        Long value
    ) {
    }
}
