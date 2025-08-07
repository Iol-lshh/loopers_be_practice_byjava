package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;

import java.util.List;

public class OrderV1Dto {
    public static class Request{
        public record Order(
                Long userId,
                List<Item> items,
                List<Long> coupons
        ) {
            public OrderCriteria.Order toCriteria() {
                List<OrderCriteria.Item> items = this.items.stream()
                        .map(item -> new OrderCriteria.Item(
                                item.productId(),
                                item.quantity()
                        )).toList();
                return new OrderCriteria.Order(userId, items, coupons);
            }
        }

        public record Item(
                Long productId,
                Long quantity
        ) {
        }
    }

    public static class Response {
        public record Summary(
                              Long orderId,
                              Long userId,
                              String orderDate,
                              long totalPrice
        ) {
            public static Summary from(OrderResult.Summary order) {
                return new Summary(
                        order.orderId(),
                        order.userId(),
                        order.orderDate(),
                        order.totalPrice()
                );
            }

            public static List<Summary> of(List<OrderResult.Summary> result) {
                return result.stream()
                        .map(Summary::from)
                        .toList();
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
            public static Detail from(OrderResult.Detail order) {
                List<Item> itemInfos = order.items().stream()
                        .map(item -> new Item(
                                item.productId(),
                                item.quantity(),
                                item.price()
                        )).toList();
                List<Coupon> couponInfos = order.coupons().stream()
                        .map(coupon -> new Coupon(
                                coupon.couponId(),
                                coupon.value()
                        )).toList();
                return new Detail(
                        order.orderId(),
                        order.userId(),
                        order.orderDate(),
                        order.totalPrice(),
                        itemInfos,
                        couponInfos
                );
            }
        }

        public record Item(
                Long productId,
                Long quantity,
                Long price
        ) {
            public Item(OrderResult.Item item) {
                this(item.productId(), item.quantity(), item.price());
            }
        }

        public record Coupon(
                Long id,
                Long value
        ) {
            public Coupon(OrderResult.Coupon coupon) {
                this(coupon.couponId(), coupon.value());
            }
        }
    }
}
