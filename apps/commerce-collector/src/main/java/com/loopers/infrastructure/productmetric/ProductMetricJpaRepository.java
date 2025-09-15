package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricEntity;
import com.loopers.domain.productmetric.ProductMetricInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductMetricJpaRepository extends JpaRepository<ProductMetricEntity, Long> {

    @Query("SELECT p FROM ProductMetricEntity p WHERE p.productId IN :productIds AND p.metricName = :metric")
    List<ProductMetricEntity> findAllByProductIds(List<Long> productIds, String metric);

    Optional<ProductMetricEntity> findByProductId(Long productId);

    @Query("""
        SELECT new com.loopers.domain.productmetric.ProductMetricInfo$Aggregate(
            p.productId,
            SUM(CASE WHEN p.metricName = 'view_count' THEN CAST(p.metricValue AS long) ELSE 0 END),
            SUM(CASE WHEN p.metricName = 'like_count' THEN CAST(p.metricValue AS long) ELSE 0 END),
            SUM(CASE WHEN p.metricName = 'sold_count' THEN CAST(p.metricValue AS long) ELSE 0 END),
            SUM(CASE WHEN p.metricName = 'sold_amount' THEN CAST(p.metricValue AS long) ELSE 0 END)
        )
        FROM ProductMetricEntity p
        WHERE p.productId = :productId
        GROUP BY p.productId
    """)
    Optional<ProductMetricInfo.Aggregate> findAggregate(Long productId);

    @Query("SELECT DISTINCT p.productId FROM ProductMetricEntity p")
    List<Long> findAllDistinctProductIds();
}
