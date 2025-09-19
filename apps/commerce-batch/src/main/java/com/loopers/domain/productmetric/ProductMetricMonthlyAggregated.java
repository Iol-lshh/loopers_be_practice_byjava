package com.loopers.domain.productmetric;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetricMonthlyAggregated {
    private Long productId;
    private Long viewCount;
    private Long likeCount;
    private Long soldCount;
    private Long soldAmount;
    private Integer year;        // 2024
    private Integer month;       // 1~12
    private LocalDateTime aggregatedAt; // 집계 실행 시점
}
