package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderEntity register(OrderCommand.Order orderCommand) {
        OrderEntity order = OrderEntity.from(orderCommand);
        var result = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCommand.RequestPayment(
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
    public OrderInfo.Pay complete(OrderCommand.Complete command) {
        OrderEntity order = orderRepository.find(command.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다.: " + command.orderId()));
        order.complete();
        orderRepository.saveAndFlush(order);
        OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(command.paymentType());
        return new OrderInfo.Pay(
                command.userId(), command.userId(), command.totalPrice(), paymentType, order.getState()
        );
    }

}
