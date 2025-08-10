package com.loopers.application.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PointUsecaseIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    @Autowired
    private PointFacade pointFacade;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UserService userService;
    @MockitoSpyBean
    private PointService pointService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 조회")
    @Nested
    class Get {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void getPoints_whenUserExists() {
            // given
            var command = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            UserEntity tester = userService.create(command);

            // when
            PointResult points = pointFacade.get(tester.getId());

            // then
            assertNotNull(points);
            assertNotNull(points.amount());
            assertEquals(tester.getId(), points.userId());
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, CoreException:NOT_FOUND 가 발생한다.")
        @Test
        void throwNotFound_whenUserDoesNotExist() {
            // given
            Long nonExistentUserId = 999L;
            Optional<UserEntity> nonExistentUser = userService.find(nonExistentUserId);
            assertTrue(nonExistentUser.isEmpty());

            // when
            CoreException coreException = assertThrows(CoreException.class,
                    () -> pointFacade.get(nonExistentUserId));

            // then
            assertEquals(ErrorType.NOT_FOUND, coreException.getErrorType());
        }
    }

    @DisplayName("포인트 충전")
    @Nested
    class Charge {
        @DisplayName("존재하는 유저 ID 로 충전을 시도한 경우, 포인트 조회시 충전된 포인트가 반환된다.")
        @Test
        void chargePoints_whenUserExists() {
            // given
            var command = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            UserEntity tester = userService.create(command);
            assertNotNull(tester.getId());
            Long chargeAmount = 100L;

            // when
            PointResult result = pointFacade.charge(tester.getId(), chargeAmount);

            // then
            assertNotNull(result);
            assertNotNull(result.amount());
            assertEquals(tester.getId(), result.userId());
            assertEquals(chargeAmount, result.amount());
        }


        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void throwNotFound_whenUserDoesNotExist() {
            // given
            Long nonExistentUserId = 999L;
            Optional<UserEntity> nonExistentUser = userService.find(nonExistentUserId);
            assertTrue(nonExistentUser.isEmpty());

            // when
            CoreException coreException = assertThrows(CoreException.class,
                    () -> pointFacade.charge(nonExistentUserId, 100L));

            // then
            assertEquals(ErrorType.NOT_FOUND, coreException.getErrorType());
        }
    }

    @DisplayName("포인트 차감")
    @Nested
    class Pay {
        @DisplayName("유저가 한번에 여러 번의 포인트 차감을 시도한 경우, 순차적으로 포인트가 차감된다.")
        @Test
        void payPoints_whenMultipleUsers() throws InterruptedException {
            // given
            var command = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            UserEntity tester = userService.create(command);
            assertNotNull(tester.getId());
            Long chargeAmount = 100L;
            pointFacade.charge(tester.getId(), chargeAmount);

            Long price = 11L;

            // when
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        PointEntity point = pointService.pay(tester.getId(), price);
                        log.info("포인트 차감 성공 - {} 잔액: {}", index, point.getAmount());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("포인트 차감 실패 - {}: {}", index, e.getMessage());
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            // then
            ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
            verify(pointService, atLeast(threadCount)).pay(userIdCaptor.capture(), amountCaptor.capture());
            List<Long> capturedAmounts = amountCaptor.getAllValues();
            Long sum = capturedAmounts.stream().mapToLong(Long::longValue).sum();


            PointResult pointResult = pointFacade.get(tester.getId());
            assertNotNull(pointResult);
            assertTrue(pointResult.amount() >= 0);
            assertEquals(chargeAmount - price * successCount.longValue(), pointResult.amount());
            log.info("포인트 차감 요청 횟수: {}", capturedAmounts.size());
            log.info("포인트 총 차감 요청 금액: {}", sum);
            log.info("최종 포인트 잔액: {}", pointResult.amount());
        }

        @DisplayName("포인트 충전과 차감이 동시에 발생하는 경우, 정상적으로 모두 반영된다.")
        @Test
        void chargeAndPayPointsConcurrently() throws InterruptedException {
            // given
            var command = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            UserEntity tester = userService.create(command);
            assertNotNull(tester.getId());
            Long chargeAmount = 100L;
            pointFacade.charge(tester.getId(), chargeAmount);

            Long price = 10L;

            // when
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        if(index % 2 == 0) {
                            // 충전
                            PointEntity point = pointService.charge(tester.getId(), price);
                            log.info("포인트 충전 성공 - {} 잔액: {}", index, point.getAmount());
                        } else {
                            // 차감
                            if (pointService.findByUserId(tester.getId()).isPresent()) {
                                PointEntity point = pointService.pay(tester.getId(), price);
                                log.info("포인트 차감 성공 - {} 잔액: {}", index, point.getAmount());
                            } else {
                                throw new CoreException(ErrorType.BAD_REQUEST, "충전된 포인트가 없습니다.");
                            }
                        }
                    } catch (Exception e) {
                        log.error("포인트 차감 실패 - {}: {}", index, e.getMessage());
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            PointResult pointResult = pointFacade.get(tester.getId());
            assertNotNull(pointResult);
            assertTrue(pointResult.amount() >= 0);
            log.info("포인트 충전 요청 횟수: {}", successCount.get());
            log.info("포인트 차감 요청 횟수: {}", failureCount.get());
            log.info("최종 포인트 잔액: {}", pointResult.amount());
        }
    }
}
