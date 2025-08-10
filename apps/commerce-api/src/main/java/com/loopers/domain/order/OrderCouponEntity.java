package com.loopers.domain.order;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Table(name = "order_coupon")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
public class OrderCouponEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Id
    private Long couponId;
    private Long value;


    public OrderCouponEntity(Long couponId, Long value, OrderEntity order) {
        this.couponId = couponId;
        this.value = value;
        this.order = order;
    }

    public static List<OrderCouponEntity> from(OrderEntity orderEntity, List<OrderCommand.Coupon> coupons) {
        return coupons.stream()
                .map(coupon -> new OrderCouponEntity(coupon.id(), coupon.value(), orderEntity))
                .toList();
    }

    @EqualsAndHashCode
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @Embeddable
    public static class OrderCouponId implements Serializable {
        private Long order;
        private Long couponId;
    }
}
