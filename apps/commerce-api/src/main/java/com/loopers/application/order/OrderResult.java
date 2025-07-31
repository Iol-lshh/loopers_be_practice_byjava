package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;

public class OrderResult {
    public record Summary(
        Long orderId,
        Long userId,
        String orderDate,
        long totalPrice
    ) {
        public static Summary from(OrderEntity order) {
            return new Summary(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalPrice()
            );
        }
    }

    public record Detail(
        Long orderId,
        Long userId,
        String orderDate,
        long totalPrice,
        List<OrderItemInfo> items
    ) {
        public static Detail from(OrderEntity order, List<ProductEntity> products) {
            List<OrderItemInfo> itemInfos = order.getOrderItems().stream()
                .map(item -> {
                    ProductEntity product = products.stream()
                        .filter(p -> p.getId().equals(item.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다: " + item.getProductId()));
                    return new OrderItemInfo(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        item.getPrice()
                    );
                })
                .toList();

            return new Detail(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalPrice(),
                itemInfos
            );
        }
    }

    public record OrderItemInfo(
        Long productId,
        String productName,
        Long quantity,
        Long price
    ) {
    }
}
