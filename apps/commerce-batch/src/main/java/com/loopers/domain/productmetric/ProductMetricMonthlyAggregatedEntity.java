package com.loopers.domain.productmetric;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// 귀찮아서 테이블 만드는 용도
@Table(name = "product_metric_monthly_aggregated")
@Entity
public class ProductMetricMonthlyAggregatedEntity extends BaseEntity {
    private Long productId;
    private Long viewCount;
    private Long likeCount;
    private Long soldCount;
    private Long soldAmount;
    private Integer year;        // 2024
    private Integer month;       // 1~12
    private LocalDateTime aggregatedAt;
}
