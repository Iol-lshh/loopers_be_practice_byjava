package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricMonthlyAggregated;
import com.loopers.domain.productmetric.ProductMetricWeeklyAggregated;
import com.loopers.domain.productmetric.ProductMetricWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class ProductMetricWriterImpl implements ProductMetricWriter {

    private final DataSource dataSource;


    @Override
    public JdbcBatchItemWriter<ProductMetricWeeklyAggregated> weeklyWriter() {
        return new JdbcBatchItemWriterBuilder<ProductMetricWeeklyAggregated>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO product_metric_weekly_aggregated (
                            product_id,
                            view_count,
                            like_count,
                            sold_count,
                            sold_amount,
                            aggregated_at
                        ) VALUES (
                            :productId,
                            :viewCount,
                            :likeCount,
                            :soldCount,
                            :soldAmount,
                            NOW()
                        )
                        ON DUPLICATE KEY UPDATE
                            view_count = VALUES(view_count),
                            like_count = VALUES(like_count),
                            sold_count = VALUES(sold_count),
                            sold_amount = VALUES(sold_amount),
                            aggregated_at = NOW()
                        """)
                .beanMapped()
                .build();
    }

    @Override
    public ItemWriter<ProductMetricMonthlyAggregated> monthlyWriter() {
        return new JdbcBatchItemWriterBuilder<ProductMetricMonthlyAggregated>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO product_metric_monthly_aggregated (
                            product_id,
                            view_count,
                            like_count,
                            sold_count,
                            sold_amount,
                            aggregated_at
                        ) VALUES (
                            :productId,
                            :viewCount,
                            :likeCount,
                            :soldCount,
                            :soldAmount,
                            NOW()
                        )
                        ON DUPLICATE KEY UPDATE
                            view_count = VALUES(view_count),
                            like_count = VALUES(like_count),
                            sold_count = VALUES(sold_count),
                            sold_amount = VALUES(sold_amount),
                            aggregated_at = NOW()
                        """)
                .beanMapped()
                .build();
    }
}
