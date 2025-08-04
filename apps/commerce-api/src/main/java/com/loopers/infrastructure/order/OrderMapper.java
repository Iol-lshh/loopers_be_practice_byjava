package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public Map<Long, Long> getItemQuantityMap(OrderEntity order) {
        return order.getOrderItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProductId(),
                        item -> item.getQuantity()
                ));
    }
}
