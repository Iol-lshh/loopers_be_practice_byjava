package com.loopers.domain.productmetric;

import java.time.LocalDateTime;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

// 귀찮아서 테이블 만드는 용도
@Table(name = "product_metric_weekly_aggregated")
@Entity
@IdClass(ProductMetricWeeklyAggregatedEntity.WeeklyId.class)
public class ProductMetricWeeklyAggregatedEntity {
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
    @Id
    private Integer weekOfMonth;
    private LocalDateTime aggregatedAt; // 집계 실행 시점

    @Embeddable
    public static class WeeklyId {
        private Long productId;
        private Integer year;
        private Integer month;
        private Integer weekOfMonth;
    }
}
