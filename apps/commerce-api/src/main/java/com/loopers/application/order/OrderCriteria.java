package com.loopers.application.order;

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
            List<Item> orderItems
    ) {
        public OrderCommand.Order toCommandWithProductPriceList(Map<Long, Long> ProductPriceList) {
            List<OrderCommand.Item> items = orderItems.stream()
                    .map(item -> new OrderCommand.Item(
                            item.productId,
                            ProductPriceList.getOrDefault(item.productId, 0L),
                            item.quantity
                    )).toList();
            return new OrderCommand.Order(userId, items);
        }

        public Map<Long, Long> getOrderItemMap() {
            return orderItems.stream()
                    .collect(Collectors.toMap(
                            OrderCriteria.Item::productId,
                            OrderCriteria.Item::quantity
                    ));
        }
    }
}
