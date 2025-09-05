package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductMetricJpaRepository extends JpaRepository<ProductMetricEntity, Long> {
    Optional<ProductMetricEntity> findByProductId(Long productId);
}
