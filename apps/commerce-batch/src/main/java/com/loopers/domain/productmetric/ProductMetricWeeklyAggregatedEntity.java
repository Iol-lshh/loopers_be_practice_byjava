package com.loopers.domain.productmetric;

import java.time.LocalDateTime;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// 귀찮아서 테이블 만드는 용도
@Table(name = "product_metric_weekly_aggregated")
@Entity
public class ProductMetricWeeklyAggregatedEntity extends BaseEntity {
    private Long productId;
    private Long viewCount;
    private Long likeCount;
    private Long soldCount;
    private Long soldAmount;
    private Integer year;        // 2024
    private Integer month;       // 1~12
    private Integer weekOfMonth; 
    private LocalDateTime aggregatedAt; // 집계 실행 시점
}
