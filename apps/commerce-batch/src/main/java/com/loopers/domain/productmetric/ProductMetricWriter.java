package com.loopers.domain.productmetric;

import org.springframework.batch.item.ItemWriter;

public interface ProductMetricWriter {
    ItemWriter<ProductMetricWeeklyAggregated> weeklyWriter();
    ItemWriter<ProductMetricMonthlyAggregated> monthlyWriter();
    void cacheRanking();
}
