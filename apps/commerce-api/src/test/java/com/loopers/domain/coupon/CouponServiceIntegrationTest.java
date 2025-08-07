package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CouponServiceIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private CouponService couponService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private static final Logger log = LoggerFactory.getLogger(CouponServiceIntegrationTest.class);


    @DisplayName("쿠폰 등록")
    @Nested
    class Register {
        @DisplayName("정상적인 값들이 제공될 때, 쿠폰이 등록된다.")
        @Test
        void returnCoupon_WhenValidValuesProvided() {
            // given
            CouponCommand.Admin.Create command = new CouponCommand.Admin.Create("FIXED", 1000L);

            // when
            CouponEntity coupon = couponService.register(command);

            // then
            assertNotNull(coupon);
            assertNotNull(coupon.getId());
            assertEquals(CouponEntity.Type.FIXED, coupon.getType());
            assertEquals(1000L, coupon.getValue());
        }
    }

    @DisplayName("쿠폰 조회")
    @Nested
    class Find {
        @DisplayName("등록된 쿠폰이 존재할 때, 해당 쿠폰을 조회한다.")
        @Test
        void returnCoupon_WhenCouponExists() {
            // given
            CouponCommand.Admin.Create command = new CouponCommand.Admin.Create("FIXED", 1000L);
            CouponEntity coupon = couponService.register(command);

            // when
            CouponEntity foundCoupon = couponService.find(coupon.getId()).orElse(null);

            // then
            assertNotNull(foundCoupon);
            assertEquals(coupon.getId(), foundCoupon.getId());
            assertEquals(CouponEntity.Type.FIXED, foundCoupon.getType());
            assertEquals(1000L, foundCoupon.getValue());
        }
    }

    @DisplayName("쿠폰 맵 조회")
    @Nested
    class GetCouponValueMap{
        @DisplayName("쿠폰 ID 리스트와 상품 가격 맵이 주어질 때, 리스트 순서에 따라 반영된 쿠폰의 적용된 값 맵을 반환한다.")
        @Test
        void returnCouponValueMap_WhenValidCouponIdsAndProductPricesProvided() {
            // given
            CouponCommand.Admin.Create command1 = new CouponCommand.Admin.Create("PERCENTAGE", 10L);
            CouponCommand.Admin.Create command2 = new CouponCommand.Admin.Create("FIXED", 1000L);
            CouponEntity coupon1 = couponService.register(command1);
            CouponEntity coupon2 = couponService.register(command2);

            Map<Long, Long> orderItemMap = Map.of(
                    1L, 4L,
                    2L, 4L
            );

            Map<Long, Long> productPriceMap = Map.of(
                    1L, 5000L,
                    2L, 20000L
            ); // total price = 4 * 5000 + 4 * 20000 = 100000L

            CouponCommand.User.Order orderCommand = new CouponCommand.User.Order(
                    1L, List.of(coupon1.getId(), coupon2.getId()));

            // when
            Map<Long, Long> couponValueMap = couponService.getCouponValueMap(
                    orderCommand, orderItemMap, productPriceMap);

            // then
            assertNotNull(couponValueMap);
            assertEquals(2, couponValueMap.size());
            assertEquals(10000L, couponValueMap.get(coupon1.getId()));
            assertEquals(1000L, couponValueMap.get(coupon2.getId()));
        }
    }

    @DisplayName("쿠폰 사용")
    @Nested
    class UseCoupons {
        @DisplayName("사용 가능한 쿠폰 ID 리스트가 주어질 때, 해당 쿠폰들을 사용하고 쿠폰 리스트를 반환한다.")
        @Test
        void returnUsedCoupons_WhenValidCouponIdsProvided() {
            // given
            CouponCommand.Admin.Create command1 = new CouponCommand.Admin.Create("FIXED", 1000L);
            CouponCommand.Admin.Create command2 = new CouponCommand.Admin.Create("PERCENTAGE", 10L);
            CouponEntity coupon1 = couponService.register(command1);
            CouponEntity coupon2 = couponService.register(command2);

            // when
            List<CouponEntity> usedCoupons = couponService.useCoupons(1L, List.of(coupon1.getId(), coupon2.getId()));

            // then
            assertNotNull(usedCoupons);
            assertEquals(2, usedCoupons.size());
            assertTrue(usedCoupons.stream().anyMatch(c -> c.getId().equals(coupon1.getId())));
            assertTrue(usedCoupons.stream().anyMatch(c -> c.getId().equals(coupon2.getId())));
        }

        @DisplayName("이미 사용된 쿠폰 ID가 포함된 경우, 예외가 발생한다.")
        @Test
        void throwException_WhenUsedCouponIdProvided() {
            // given
            CouponCommand.Admin.Create command = new CouponCommand.Admin.Create("FIXED", 1000L);
            CouponEntity coupon = couponService.register(command);
            couponService.useCoupons(1L, List.of(coupon.getId()));

            // when
            var result = assertThrows(CoreException.class, () -> {
                couponService.useCoupons(1L, List.of(coupon.getId()));
            });

            // then
            assertEquals(ErrorType.CONFLICT, result.getErrorType());
        }

        @DisplayName("존재하지 않는 쿠폰 ID가 포함된 경우, 예외가 발생한다.")
        @Test
        void throwException_WhenNonExistentCouponIdProvided() {
            // given
            Long nonExistentCouponId = 999L;
            assertTrue(couponService.find(nonExistentCouponId).isEmpty());

            // when
            var result = assertThrows(CoreException.class, () -> {
                couponService.useCoupons(1L, List.of(nonExistentCouponId));
            });

            // then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

        @DisplayName("쿠폰을 동시에 사용할 때, 중복 사용이 방지된다.")
        @Test
        void preventDuplicateUsage_WhenCouponsUsedConcurrently() throws InterruptedException {
            // given
            CouponCommand.Admin.Create command = new CouponCommand.Admin.Create("FIXED", 1000L);
            CouponEntity coupon = couponService.register(command);
            Long userId = 1L;

            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            AtomicInteger exceptionCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            List<CouponEntity> results = new ArrayList<>();


            // when
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        List<CouponEntity> result = couponService.useCoupons(userId, List.of(coupon.getId()));
                        results.add(result.getFirst());
                        successCount.incrementAndGet();
                    } catch (CoreException e) {
                        exceptionCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // then
            assertTrue(couponService.find(coupon.getId()).isPresent());

            assertEquals(1, successCount.get());
            assertTrue(exceptionCount.get() > 0);
            assertEquals(1, results.size());
            results.forEach(result -> log.info(result.getId().toString()));

        }
    }
}
