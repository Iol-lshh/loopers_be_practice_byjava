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
    private State state;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItemEntity> orderItems;

    private OrderEntity(Long userId, List<OrderItemEntity> orderItems) {
        super();
        this.userId = userId;
        this.orderItems = orderItems;
        this.state = State.PENDING;
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

    public void complete() {
        this.state = State.COMPLETED;
    }

    public void cancel() {
        this.state = State.CANCELLED;
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
