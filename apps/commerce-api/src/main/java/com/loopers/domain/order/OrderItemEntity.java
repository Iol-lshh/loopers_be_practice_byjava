package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "order_item")
public class OrderItemEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;
    
    private Long productId;
    private Long price;
    private Long quantity;

    protected OrderItemEntity() {}
    public OrderItemEntity(OrderEntity order, Long productId, Long price, Long quantity) {
        super();
        this.order = order;
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public static List<OrderItemEntity> from(OrderEntity order, List<OrderCommand.Item> orderItemCommands) {
        if (orderItemCommands == null || orderItemCommands.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 목록이 비어있습니다.");
        }

        return orderItemCommands.stream()
                .map(item -> new OrderItemEntity(order, item.productId(), item.price(), item.quantity()))
                .toList();
    }
}
