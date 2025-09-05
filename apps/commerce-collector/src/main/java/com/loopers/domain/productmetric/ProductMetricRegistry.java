package com.loopers.domain.productmetric;

import java.util.Optional;

public interface ProductMetricRegistry {
    ProductMetricEntity save(ProductMetricEntity entity);

    Optional<ProductMetricEntity> findByProductId(Long productId);
}
