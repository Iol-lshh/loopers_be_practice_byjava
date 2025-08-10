package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {
    private Long userId;
    private State state;
    @Version
    private Long version = 0L;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItemEntity> orderItems;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderCouponEntity> orderCoupons;

    private OrderEntity(Long userId) {
        super();
        this.userId = userId;
        this.state = State.PENDING;
        this.orderItems = new ArrayList<>();
        this.orderCoupons = new ArrayList<>();
    }

    public static OrderEntity from(OrderCommand.Order orderCommand) {
        OrderEntity order = new OrderEntity(orderCommand.userId());
        order.addOrderItemsByCommand(orderCommand.orderItems());
        order.addOrderCoupons(orderCommand.orderCoupons());
        return order;
    }

    protected void addOrderItemsByCommand(List<OrderCommand.Item> items){
        List<OrderItemEntity> orderItems = OrderItemEntity.from(this, items);
        this.orderItems.addAll(orderItems);
    }

    protected void addOrderCoupons(List<OrderCommand.Coupon> coupons) {
        List<OrderCouponEntity> orderCoupons = OrderCouponEntity.from(this, coupons);
        this.orderCoupons.addAll(orderCoupons);
    }

    public Long getTotalPrice() {
        long price = orderItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
        long couponDiscount = orderCoupons.stream()
                .mapToLong(OrderCouponEntity::getValue)
                .sum();
        return price > couponDiscount ? price - couponDiscount : 0;
    }

    public void complete() {
        if (state != State.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태가 PENDING이 아닙니다. 현재 상태: " + state.getDescription());
        }
        this.state = State.COMPLETED;
    }

    public void cancel() {
        this.state = State.CANCELLED;
    }

    public List<Long> getOrderedProductIds() {
        return getOrderItems().stream()
                .map(OrderItemEntity::getProductId)
                .toList();
    }

    public Map<Long, Long> getItemQuantityMap() {
        return getOrderItems().stream()
                .collect(Collectors.toMap(
                        OrderItemEntity::getProductId,
                        OrderItemEntity::getQuantity
                ));
    }

    public Map<Long, Long> getAppliedCouponValueMap() {
        return getOrderCoupons().stream()
                .collect(Collectors.toMap(
                        OrderCouponEntity::getCouponId,
                        OrderCouponEntity::getValue
                ));
    }

    public List<Long> getCouponIds() {
        return getOrderCoupons().stream()
                .map(OrderCouponEntity::getCouponId)
                .toList();
    }

    public enum State {
        PENDING("주문 대기"),
        COMPLETED("주문 완료"),
        CANCELLED("주문 취소");

        private final String description;

        State(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
