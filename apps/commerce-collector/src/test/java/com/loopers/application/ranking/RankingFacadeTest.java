package com.loopers.application.ranking;

import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.domain.productmetric.ProductMetricService;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=test"
})
class RankingFacadeTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ProductMetricService productMetricService;

    @Autowired
    private RankingFacade rankingFacade;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }


    @Test
    @DisplayName("updateAllProductToDayRankings - 전날 데이터 추가와 함께 모든 상품 랭킹 업데이트")
    void updateAllProductToDayRankings_success() {
        // Given - 실제 상품 메트릭 데이터 생성
        createProductMetricData(1L, 100L, 50L, 10L, 50000L);
        createProductMetricData(2L, 200L, 80L, 20L, 100000L);
        createProductMetricData(3L, 150L, 60L, 15L, 75000L);
        
        // 전날 랭킹 데이터 설정 (Redis에 직접 추가)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayKey = "product:ranking:" + yesterday;
        redisTemplate.opsForZSet().add(yesterdayKey, "1", 100.0);
        redisTemplate.opsForZSet().add(yesterdayKey, "2", 120.0);
        redisTemplate.opsForZSet().add(yesterdayKey, "3", 90.0);

        // When
        assertDoesNotThrow(() -> rankingFacade.updateAllProductToDayRankings());

        // Then
        LocalDate today = LocalDate.now();
        String todayKey = "product:ranking:" + today;
        
        // 최종 랭킹 데이터 확인 (전날 스코어 1/10 + 오늘 계산된 스코어)
        Double finalScore1 = redisTemplate.opsForZSet().score(todayKey, "1");
        Double finalScore2 = redisTemplate.opsForZSet().score(todayKey, "2");
        Double finalScore3 = redisTemplate.opsForZSet().score(todayKey, "3");
        
        assertNotNull(finalScore1);
        assertNotNull(finalScore2);
        assertNotNull(finalScore3);
        
        // 전날 데이터가 1/10로 추가되고 새로운 랭킹이 계산되었는지 확인
        assertTrue(finalScore1 > 10.0); // 전날 스코어(100/10) + 새로운 스코어
        assertTrue(finalScore2 > 12.0); // 전날 스코어(120/10) + 새로운 스코어  
        assertTrue(finalScore3 > 9.0);  // 전날 스코어(90/10) + 새로운 스코어
    }

    @Test
    @DisplayName("updateAllProductToDayRankings - 전날 데이터가 없는 경우")
    void updateAllProductToDayRankings_noYesterdayData() {
        // Given - 실제 상품 메트릭 데이터 생성
        createProductMetricData(1L, 100L, 50L, 10L, 50000L);
        createProductMetricData(2L, 200L, 80L, 20L, 100000L);
        
        // 전날 데이터는 설정하지 않음

        // When
        assertDoesNotThrow(() -> rankingFacade.updateAllProductToDayRankings());

        // Then
        LocalDate today = LocalDate.now();
        String todayKey = "product:ranking:" + today;
        
        Double finalScore1 = redisTemplate.opsForZSet().score(todayKey, "1");
        Double finalScore2 = redisTemplate.opsForZSet().score(todayKey, "2");
        
        assertNotNull(finalScore1);
        assertNotNull(finalScore2);
        // 전날 데이터가 없으므로 새로 계산된 스코어만 있어야 함
        assertTrue(finalScore1 > 0);
        assertTrue(finalScore2 > 0);
    }

    @Test
    @DisplayName("updateAllProductRankings - 기본 랭킹 업데이트 (전날 데이터 추가 없음)")
    void updateAllProductRankings_success() {
        // Given - 실제 상품 메트릭 데이터 생성
        createProductMetricData(1L, 100L, 50L, 10L, 50000L);
        createProductMetricData(2L, 200L, 80L, 20L, 100000L);

        // When
        assertDoesNotThrow(() -> rankingFacade.updateAllProductRankings());

        // Then
        LocalDate today = LocalDate.now();
        String todayKey = "product:ranking:" + today;
        
        Double finalScore1 = redisTemplate.opsForZSet().score(todayKey, "1");
        Double finalScore2 = redisTemplate.opsForZSet().score(todayKey, "2");
        
        assertNotNull(finalScore1);
        assertNotNull(finalScore2);
        // 계산된 스코어만 있어야 함 (전날 데이터 추가 없음)
        assertTrue(finalScore1 > 0);
        assertTrue(finalScore2 > 0);
    }

    @Test
    @DisplayName("updateAllProductRankings - 상품 목록이 비어있는 경우")
    void updateAllProductRankings_emptyProductList() {
        // Given - 상품 메트릭 데이터 없음

        // When
        assertDoesNotThrow(() -> rankingFacade.updateAllProductRankings());

        // Then
        LocalDate today = LocalDate.now();
        String todayKey = "product:ranking:" + today;
        
        // 랭킹 데이터가 비어있어야 함
        Long rankingCount = redisTemplate.opsForZSet().count(todayKey, 0, Double.MAX_VALUE);
        assertEquals(0, rankingCount);
    }

    /**
     * 테스트용 상품 메트릭 데이터를 생성하는 헬퍼 메서드
     */
    private void createProductMetricData(Long productId, Long viewCount, Long likeCount, Long soldCount, Long soldAmount) {
        try {
            // view_count 메트릭 추가 (단순하게 생성만)
            productMetricService.addViewCount(new ProductMetricCommand.AddViewCount(productId, viewCount));
        } catch (Exception e) {
            // view_count 추가에 실패해도 계속 진행
            System.out.println("View count 추가 실패: " + e.getMessage());
        }
        
        try {
            // like_count 메트릭 추가
            productMetricService.updateLikeCount(new ProductMetricCommand.UpdateLikeCount(productId, likeCount));
        } catch (Exception e) {
            // like_count 추가에 실패해도 계속 진행
            System.out.println("Like count 추가 실패: " + e.getMessage());
        }
        
        try {
            // sold_count와 sold_amount 메트릭 추가
            productMetricService.addSoldCount(
                List.of(new ProductMetricCommand.AddSoldCount(productId, soldCount)),
                List.of(new ProductMetricCommand.AddSoldAmount(productId, soldAmount))
            );
        } catch (Exception e) {
            // sold count/amount 추가에 실패해도 계속 진행
            System.out.println("Sold count/amount 추가 실패: " + e.getMessage());
        }
    }
}
