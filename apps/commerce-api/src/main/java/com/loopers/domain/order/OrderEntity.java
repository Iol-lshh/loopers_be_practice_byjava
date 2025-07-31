package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {
    private Long userId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItemEntity> orderItems;

    private OrderEntity(Long userId, List<OrderItemEntity> orderItems) {
        super();
        this.userId = userId;
        this.orderItems = orderItems;
    }

    public static OrderEntity from(OrderCommand.Order orderCommand) {
        OrderEntity order = new OrderEntity(orderCommand.userId(), null);
        order.orderItems = OrderItemEntity.from(order, orderCommand.orderItems());
        return order;
    }

    public Long getTotalPrice() {
        return orderItems.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
