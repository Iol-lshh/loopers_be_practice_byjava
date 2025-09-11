package com.loopers.domain.productmetric;

import java.util.List;
import java.util.Optional;

public interface ProductMetricRegistry {
    ProductMetricEntity save(ProductMetricEntity entity);

    Optional<ProductMetricEntity> findByProductIdAndMetric(Long productId, String metric);
    List<ProductMetricEntity> findAllByProductIdsAndMetric(List<Long> productIds, String metric);

    List<ProductMetricEntity> saveAll(List<ProductMetricEntity> entities);

    Optional<ProductMetricInfo.Aggregate> findAggregate(Long productId);
}
