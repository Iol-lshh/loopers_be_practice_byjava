package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricEntity;
import com.loopers.domain.productmetric.ProductMetricInfo;
import com.loopers.domain.productmetric.ProductMetricRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMetricRegistryImpl implements ProductMetricRegistry {

    private final ProductMetricJpaRepository jpaRepository;

    @Override
    public ProductMetricEntity save(ProductMetricEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<ProductMetricEntity> findByProductIdAndMetric(Long productId, String metric) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductMetricEntity> findAllByProductIdsAndMetric(List<Long> productIds, String metric) {
        return jpaRepository.findAllByProductIds(productIds, metric);
    }

    @Override
    public List<ProductMetricEntity> saveAll(List<ProductMetricEntity> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    public Optional<ProductMetricInfo.Aggregate> findAggregate(Long productId) {
        return jpaRepository.findAggregate(productId);
    }

    @Override
    public List<Long> findAllDistinctProductIds() {
        return jpaRepository.findAllDistinctProductIds();
    }

}
