package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderStatement;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;

    @Override
    public OrderEntity save(OrderEntity order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<OrderEntity> find(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<OrderEntity> find(OrderStatement orderStatement) {
        Specification<OrderEntity> spec = OrderJpaSpecification.with(orderStatement);
        return jpaRepository.findAll(spec);
    }
}
