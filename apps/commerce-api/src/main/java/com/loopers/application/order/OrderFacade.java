package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final PointService pointService;

    @Transactional
    public OrderResult.Summary orderByPoint(OrderCriteria.Order criteria) {
        UserEntity user = userService.find(criteria.userId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + criteria.userId()));

        Map<Long, Long> orderQuantityList = criteria.orderItems().stream()
                .collect(Collectors.toMap(
                        OrderCriteria.Item::productId,
                        OrderCriteria.Item::quantity
                ));

        Map<Long, Long> productPriceList = productService.deduct(orderQuantityList);
        if (productPriceList.size() != criteria.orderItems().size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "주문할 상품이 존재하지 않습니다.");
        }

        var orderCommand = criteria.toCommandWithProductPriceList(productPriceList);
        OrderEntity order = orderService.register(orderCommand);

        pointService.deduct(user.getId(), order.getTotalPrice());

        return OrderResult.Summary.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResult.Summary> list(Long userId) {
        UserEntity user = userService.find(userId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + userId));

        var criteria = OrderStatement.userId(user.getId());
        List<OrderEntity> orders = orderService.find(criteria);
        return orders.stream()
                .map(OrderResult.Summary::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResult.Detail detail(Long userId, Long orderId) {
        UserEntity user = userService.find(userId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "User가 존재하지 않습니다: " + userId));

        OrderEntity order = orderService.find(orderId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "주문이 존재하지 않습니다: " + orderId));

        if (!order.getUserId().equals(user.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 주문을 조회할 권한이 없습니다.");
        }

        var productIds = order.getOrderItems().stream()
                .map(OrderItemEntity::getProductId)
                .toList();
        List<ProductEntity> products = productService.find(productIds);

        return OrderResult.Detail.from(order, products);
    }
}
