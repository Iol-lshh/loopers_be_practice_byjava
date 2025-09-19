package com.loopers.domain.productmetric;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// 귀찮아서 테이블 만드는 용도
@Table(name = "product_metric_monthly_aggregated")
@Entity
@IdClass(ProductMetricMonthlyAggregatedEntity.MonthlyId.class)
public class ProductMetricMonthlyAggregatedEntity {
    @Id
    private Long productId;
    private Long viewCount;
    private Long likeCount;
    private Long soldCount;
    private Long soldAmount;
    @Id
    private Integer year;        // 2024
    @Id
    private Integer month;       // 1~12
    private LocalDateTime aggregatedAt;

    // 복합키를 위한 내부 클래스
    @Embeddable
    public static class MonthlyId {
        private Long productId;
        private Integer year;
        private Integer month;
    }
}
