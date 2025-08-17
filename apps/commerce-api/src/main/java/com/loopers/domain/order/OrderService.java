package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderService {

    private final OrderRepository orderRepository;
    private final Map<OrderEntity.PaymentType, PaymentWay<?>> paymentWayMap;

    public OrderService(OrderRepository orderRepository, List<PaymentWay<?>> paymentWays) {
        this.orderRepository = orderRepository;
        this.paymentWayMap = paymentWays.stream()
                .collect(Collectors.toMap(PaymentWay::getType, paymentWay -> paymentWay));
    }

    @Transactional
    public OrderEntity register(OrderCommand.Order orderCommand) {
        OrderEntity order = OrderEntity.from(orderCommand);
        OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(orderCommand.paymentType());
        PaymentWay<?> service = paymentWayMap.get(paymentType);
        service.request(orderCommand.userId(), order.getId(), order.getTotalPrice());
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

    @Transactional
    public OrderInfo.PaymentInfo pay(OrderCommand.Pay command) {
        OrderEntity order = orderRepository.find(command.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다.: " + command.orderId()));
        order.complete();
        orderRepository.saveAndFlush(order);
        OrderEntity.PaymentType paymentType = OrderEntity.PaymentType.of(command.paymentType());
        PaymentWay<?> service = paymentWayMap.get(paymentType);
        service.pay(command.userId(), command.orderId(), command.totalPrice());
        return new OrderInfo.PaymentInfo(command.userId(), command.userId(), command.totalPrice(), paymentType, order.getState());
    }
}
