package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderEntity register(OrderCommand.Order orderCommand) {
        OrderEntity order = OrderEntity.from(orderCommand);
        var result = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderEvent.Registered(
                orderCommand.userId(),
                result.getId(),
                order.getTotalPrice(),
                orderCommand.paymentType()
        ));
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<OrderEntity> find(Long id) {
        return orderRepository.find(id);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> find(OrderStatement orderStatement) {
        return orderRepository.find(orderStatement);
    }

    @Transactional
    public OrderEntity complete(OrderCommand.Complete command) {
        OrderEntity order = orderRepository.find(command.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다.: " + command.orderId()));
        order.complete();
        OrderEvent.Completed event = OrderEvent.Completed.from(order);
        eventPublisher.publishEvent(event);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity cancel(OrderCommand.Cancel command) {
        OrderEntity order = orderRepository.find(command.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다.: " + command.orderId()));
        // 주문 취소 vs 환불 정책?
        order.cancel();
        log.info("주문 취소 상태 변경 - orderId: {}, 변경된 상태: {}", order.getId(), order.getState());
        OrderEvent.Canceled event = OrderEvent.Canceled.from(order);
        eventPublisher.publishEvent(event);
        return orderRepository.save(order);
    }
}
