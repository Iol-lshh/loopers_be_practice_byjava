package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductStatement;
import org.springframework.data.domain.Pageable;

public class ProductCacheKeyGenerator {
    public static String withSignalFrom(ProductStatement statement, Pageable pageable) {
        StringBuilder keyBuilder = new StringBuilder("product_with_signal-v1:");

        if (statement.getBrandId() != null) {
            keyBuilder.append("brand-").append(statement.getBrandId()).append(":");
        }

        if (statement.getOrderBy() != null) {
            keyBuilder.append("order-").append(statement.getOrderBy()).append(":");
        }

        keyBuilder.append("page:").append(pageable.getPageNumber()).append(":size:").append(pageable.getPageSize());

        return keyBuilder.toString();
    }
}
