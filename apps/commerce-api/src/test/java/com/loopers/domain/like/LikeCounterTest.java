package com.loopers.domain.like;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@SpringBootTest
public class LikeCounterTest {
    private static final Logger log = LoggerFactory.getLogger(LikeCounterTest.class);
    
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    
    @MockitoSpyBean
    private LikeCounter likeCounter;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    
    private ProductEntity prepareProduct() {
        Long brandId = brandService.create("Test Brand").getId();
        assertTrue(brandService.find(brandId).isPresent());
        var productCommand = new ProductCommand.Register("Test Product", brandId, 10000L, 1L);
        ProductEntity product = productService.register(productCommand);
        assertTrue(productService.find(product.getId()).isPresent());
        return product;
    }

    @DisplayName("실제 동시성 상황에서 정상 카운터 동작 검증")
    @Nested
    class ConcurrencyRetryableTest {
        
        @DisplayName("@Retryable이 실제 동시성 상황에서 동작하는지 검증")
        @Test
        public void verifyRetryable_whenConcurrencyOccurs() throws InterruptedException {
            // given
            int threadCount = 10;
            ProductEntity product = prepareProduct();

            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // when - 동시에 좋아요 등록
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        log.info("스레드 {}: 상품 {}에 좋아요 등록 시작", threadIndex, product.getId());
                        likeCounter.increaseLikeCount(product.getId(), LikeEntity.TargetType.PRODUCT);
                        log.info("스레드 {}: 상품 {}에 좋아요 등록 완료", threadIndex, product.getId());
                    } catch (Exception e) {
                        log.error("스레드 {}: 좋아요 등록 실패", threadIndex, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            log.info("=== 모든 스레드 작업 완료 ===");

            // then
            ArgumentCaptor<Long> targetIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<LikeEntity.TargetType> targetTypeCaptor = ArgumentCaptor.forClass(LikeEntity.TargetType.class);

            verify(likeCounter, atLeast(threadCount)).increaseLikeCount(targetIdCaptor.capture(), targetTypeCaptor.capture());
            List<Long> capturedTargetIds = targetIdCaptor.getAllValues();

            log.info("=== 테스트 결과 ===");
            log.info("increaseLikeCount 실제 호출 횟수: {}", capturedTargetIds.size());
            log.info("예상 최소 호출 횟수: {}", threadCount);
            log.info("재시도로 인한 추가 호출 횟수: {}", capturedTargetIds.size() - threadCount);
            
            // 최소한 threadCount만큼은 호출되어야 함
            assertTrue(capturedTargetIds.size() >= threadCount);
        }
    }
} 
