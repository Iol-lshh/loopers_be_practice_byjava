package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.domain.productmetric.ProductMetricEntity;
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
    public Optional<ProductMetricEntity> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductMetricEntity> findAllByProductIds(List<ProductMetricCommand.AddSoldCount> productId) {
        List<Long> ids = productId.stream().map(ProductMetricCommand.AddSoldCount::productId).toList();
        return jpaRepository.findAllByProductIds(ids);
    }

    @Override
    public List<ProductMetricEntity> saveAll(List<ProductMetricEntity> entities) {
        return jpaRepository.saveAll(entities);
    }

}
