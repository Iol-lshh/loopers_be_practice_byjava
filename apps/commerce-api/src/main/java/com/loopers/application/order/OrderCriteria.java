package com.loopers.application.order;

import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.order.OrderCommand;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderCriteria {
    public record Item(
            Long productId,
            Long quantity
    ) {
    }

    public record Order(
            Long userId,
            List<Item> orderItems,
            List<Long> orderCouponIds
    ) {
        public OrderCommand.Order toCommandWithProductPriceList(Map<Long, Long> ProductPriceList, Map<Long, Long> couponValueMap) {
            List<OrderCommand.Item> items = orderItems.stream()
                    .map(item -> new OrderCommand.Item(
                            item.productId,
                            ProductPriceList.getOrDefault(item.productId, 0L),
                            item.quantity
                    )).toList();
            List<OrderCommand.Coupon> coupons = orderCouponIds.stream()
                    .map(couponId -> new OrderCommand.Coupon(
                            couponId,
                            couponValueMap.getOrDefault(couponId, 0L)
                    )).toList();
            return new OrderCommand.Order(userId, items, coupons);
        }

        public Map<Long, Long> getOrderItemMap() {
            return orderItems.stream()
                    .collect(Collectors.toMap(
                            OrderCriteria.Item::productId,
                            OrderCriteria.Item::quantity
                    ));
        }

        public CouponCommand.User.Order getCouponCommand() {
            return new CouponCommand.User.Order(
                    userId,
                    orderCouponIds
            );
        }
    }
}
