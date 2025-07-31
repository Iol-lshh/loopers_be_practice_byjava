package com.loopers.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    OrderEntity save(OrderEntity order);

    Optional<OrderEntity> find(Long id);

    List<OrderEntity> find(OrderStatement orderStatement);
}
