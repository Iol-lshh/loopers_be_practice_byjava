package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricMonthlyAggregated;
import com.loopers.domain.productmetric.ProductMetricReader;
import com.loopers.domain.productmetric.ProductMetricWeeklyAggregated;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class ProductMetricReaderImpl implements ProductMetricReader {

    private final DataSource dataSource;

    @Override
    public ItemReader<ProductMetricWeeklyAggregated> weeklyMetricReader(Long startProductId) {
        return new JdbcCursorItemReaderBuilder<ProductMetricWeeklyAggregated>()
                .name("weeklyProductMetricReader")
                .dataSource(dataSource)
                .fetchSize(1000)
                .maxItemCount(Integer.MAX_VALUE)
                .saveState(true)
                .sql("""
                        SELECT
                            p.product_id,
                            SUM(CASE WHEN p.metric_name = 'view_count'  THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS view_count,
                            SUM(CASE WHEN p.metric_name = 'like_count'  THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS like_count,
                            SUM(CASE WHEN p.metric_name = 'sold_count'  THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS sold_count,
                            SUM(CASE WHEN p.metric_name = 'sold_amount' THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS sold_amount
                        FROM product_metrics p
                        WHERE p.metric_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
                          AND p.product_id >= %d
                        GROUP BY p.product_id
                        ORDER BY p.product_id
                        """.formatted(startProductId != null ? startProductId : 0L))
                .beanRowMapper(ProductMetricWeeklyAggregated.class)
                .build();
    }

    @Override
    public ItemReader<ProductMetricMonthlyAggregated> monthlyMetricReader(Long startProductId) {
        return new JdbcCursorItemReaderBuilder<ProductMetricMonthlyAggregated>()
                .name("monthlyProductMetricReader")
                .dataSource(dataSource)
                .fetchSize(1000)
                .maxItemCount(Integer.MAX_VALUE)
                .saveState(true)
                .sql("""
                        SELECT
                            p.product_id,
                            SUM(CASE WHEN p.metric_name = 'view_count'  THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS view_count,
                            SUM(CASE WHEN p.metric_name = 'like_count'  THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS like_count,
                            SUM(CASE WHEN p.metric_name = 'sold_count'  THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS sold_count,
                            SUM(CASE WHEN p.metric_name = 'sold_amount' THEN CAST(p.metric_value AS SIGNED) ELSE 0 END) AS sold_amount
                        FROM product_metrics p
                        WHERE p.metric_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
                          AND p.product_id >= %d
                        GROUP BY p.product_id
                        ORDER BY p.product_id
                        """.formatted(startProductId != null ? startProductId : 0L))
                .beanRowMapper(ProductMetricMonthlyAggregated.class)
                .build();
    }
}
