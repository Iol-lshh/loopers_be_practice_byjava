package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductMetricJpaRepository extends JpaRepository<ProductMetricEntity, Long> {

    @Query("SELECT p FROM ProductMetricEntity p WHERE p.productId IN :productIds")
    List<ProductMetricEntity> findAllByProductIds(List<Long> productIds);

    Optional<ProductMetricEntity> findByProductId(Long productId);
}
