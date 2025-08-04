package com.loopers.domain.order;

import com.loopers.application.order.OrderResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    public OrderResult.Summary complete(long orderId) {
        OrderEntity order = orderRepository.find(orderId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다.: " + orderId));

        order.complete();
        var result = orderRepository.save(order);

        return OrderResult.Summary.from(result);
    }
}
