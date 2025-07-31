package com.loopers.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderEntity register(OrderCommand.Order orderCommand) {
        OrderEntity order = OrderEntity.from(orderCommand);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<OrderEntity> find(Long id) {
        return orderRepository.find(id);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> find(OrderStatement orderStatement) {
        return orderRepository.find(orderStatement);
    }
}
