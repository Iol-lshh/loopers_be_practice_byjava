package com.loopers.domain.productmetric;

import java.util.List;
import java.util.Optional;

public interface ProductMetricRegistry {
    ProductMetricEntity save(ProductMetricEntity entity);

    Optional<ProductMetricEntity> findByProductId(Long productId);
    List<ProductMetricEntity> findAllByProductIds(List<ProductMetricCommand.AddSoldCount> productId);

    List<ProductMetricEntity> saveAll(List<ProductMetricEntity> entities);
}
