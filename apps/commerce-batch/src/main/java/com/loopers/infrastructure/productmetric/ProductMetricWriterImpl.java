package com.loopers.infrastructure.productmetric;

import com.loopers.domain.productmetric.ProductMetricMonthlyAggregated;
import com.loopers.domain.productmetric.ProductMetricWeeklyAggregated;
import com.loopers.domain.productmetric.ProductMetricWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMetricWriterImpl implements ProductMetricWriter {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;


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

    @Override
    public void cacheRanking() {
        log.info("랭킹 데이터 캐시 시작");
        
        LocalDate today = LocalDate.now();
        
        // 주간 랭킹 캐시 (현재 주차 기준)
        cacheWeeklyRanking(today);
        
        // 월간 랭킹 캐시 (현재 월 기준)
        cacheMonthlyRanking(today);
        
        log.info("랭킹 데이터 캐시 완료");
    }
    
    private void cacheWeeklyRanking(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String rankingKey = "product:ranking:" + year + month + weekOfYear;
        
        // 주간 집계 데이터를 기반으로 랭킹 계산
        String sql = """
            SELECT 
                product_id,
                (
                    COALESCE(view_count, 0) * 0.1 +
                    COALESCE(like_count, 0) * 0.3 +
                    COALESCE(sold_count, 0) * 0.4 +
                    COALESCE(sold_amount, 0) * 0.2
                ) AS ranking_score
            FROM product_metric_weekly_aggregated
            WHERE year = ? AND month = ? AND week_of_month = ?
            HAVING ranking_score > 0
            ORDER BY ranking_score DESC
            """;
            
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, year, month, weekOfYear);
        
        // Redis ZSet에 저장
        redisTemplate.delete(rankingKey);
        for (Map<String, Object> result : results) {
            Long productId = ((Number) result.get("product_id")).longValue();
            Double score = ((Number) result.get("ranking_score")).doubleValue();
            redisTemplate.opsForZSet().add(rankingKey, productId.toString(), score);
        }
        
        log.info("주간 랭킹 캐시 완료: {} 건, 키: {}", results.size(), rankingKey);
    }
    
    private void cacheMonthlyRanking(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        String rankingKey = "product:ranking:" + year + month;
        
        // 월간 집계 데이터를 기반으로 랭킹 계산
        String sql = """
            SELECT 
                product_id,
                (
                    COALESCE(view_count, 0) * 0.1 +
                    COALESCE(like_count, 0) * 0.3 +
                    COALESCE(sold_count, 0) * 0.4 +
                    COALESCE(sold_amount, 0) * 0.2
                ) AS ranking_score
            FROM product_metric_monthly_aggregated
            WHERE year = ? AND month = ?
            HAVING ranking_score > 0
            ORDER BY ranking_score DESC
            """;
            
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, year, month);
        
        // Redis ZSet에 저장
        redisTemplate.delete(rankingKey);
        for (Map<String, Object> result : results) {
            Long productId = ((Number) result.get("product_id")).longValue();
            Double score = ((Number) result.get("ranking_score")).doubleValue();
            redisTemplate.opsForZSet().add(rankingKey, productId.toString(), score);
        }
        
        log.info("월간 랭킹 캐시 완료: {} 건, 키: {}", results.size(), rankingKey);
    }
}
