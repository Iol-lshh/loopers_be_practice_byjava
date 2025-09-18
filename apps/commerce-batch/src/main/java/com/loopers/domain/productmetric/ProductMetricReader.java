package com.loopers.domain.productmetric;

import org.springframework.batch.item.ItemReader;

public interface ProductMetricReader {
    ItemReader<ProductMetricWeeklyAggregated> weeklyMetricReader(Long startProductId);
    ItemReader<ProductMetricMonthlyAggregated> monthlyMetricReader(Long startProductId);
}
