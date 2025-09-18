package com.loopers.job.productmetric;

import com.loopers.domain.productmetric.ProductMetricMonthlyAggregated;
import com.loopers.domain.productmetric.ProductMetricWeeklyAggregated;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class ProductMetricAggregateBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    public Job productMetricAggregateJob() {
        return new JobBuilder("productMetricWeeklyAggregateJob", jobRepository)
                .start(weeklyAggregateStep())
                .next(monthlyAggregateStep())
                .build();
    }

    @Bean
    public Step weeklyAggregateStep() {
        return new StepBuilder("weeklyAggregateStep", jobRepository)
                .<ProductMetricWeeklyAggregated, ProductMetricWeeklyAggregated>chunk(1000, transactionManager)
                .reader(weeklyMetricReader())
                .writer(weeklyAggregateWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<ProductMetricWeeklyAggregated> weeklyMetricReader(
    ) {
        return new JdbcCursorItemReaderBuilder<ProductMetricWeeklyAggregated>()
                .name("productMetricReader")
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
                        GROUP BY p.product_id
                        ORDER BY p.product_id
                        """)
                .beanRowMapper(ProductMetricWeeklyAggregated.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<ProductMetricWeeklyAggregated> weeklyAggregateWriter() {
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

    @Bean
    public Step monthlyAggregateStep() {
        return new StepBuilder("monthlyAggregateStep", jobRepository)
                .<ProductMetricMonthlyAggregated, ProductMetricMonthlyAggregated>chunk(1000, transactionManager)
                .reader(monthlyMetricReader())
                .writer(monthlyAggregateWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<ProductMetricMonthlyAggregated> monthlyMetricReader(
    ) {
        return new JdbcCursorItemReaderBuilder<ProductMetricMonthlyAggregated>()
                .name("productMetricReader")
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
                        GROUP BY p.product_id
                        ORDER BY p.product_id
                        """)
                .beanRowMapper(ProductMetricMonthlyAggregated.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<ProductMetricMonthlyAggregated> monthlyAggregateWriter() {
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
