package com.loopers.infrastructure.payment;

import com.loopers.resilience.AbstractResilienceTest;
import com.loopers.support.resilience.ResilienceConstant;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PgTimeoutTest extends AbstractResilienceTest {
    
    private static final Logger log = LoggerFactory.getLogger(PgTimeoutTest.class);
    
    @Test
    @DisplayName("PG_FIND_TL와 PG_REQUEST_TL TimeLimiter가 정상적으로 등록되어야 한다")
    public void shouldHaveTimeLimitersConfigured() {
        // Then
        assertThat(timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_FIND_TL)).isNotNull();
        assertThat(timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_REQUEST_TL)).isNotNull();
    }

    @DisplayName("PG_FIND_TL")
    @Nested
    class PgFindTl {
        @Test
        @DisplayName("실제 등록된 PG_FIND_TL TimeLimiter의 설정을 확인해야 한다")
        public void shouldCheckActualPgFindTimeLimiterConfiguration() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_FIND_TL);
            var config = timeLimiter.getTimeLimiterConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_FIND_TL 실제 설정 ===");
            log.info("timeoutDuration: " + config.getTimeoutDuration().toMillis() + "ms");
            log.info("cancelRunningFuture: " + config.shouldCancelRunningFuture());
            log.info("==========================");

            // 설정값 검증
            assertThat(config).isNotNull();
            assertThat(config.getTimeoutDuration().toMillis()).isGreaterThan(0);
        }

        @Test
        @DisplayName("PG_FIND_TL TimeLimiter가 timeout 설정값을 초과하면 TimeoutException이 발생해야 한다")
        public void shouldThrowTimeoutExceptionForPgFind() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_FIND_TL);
            var config = timeLimiter.getTimeLimiterConfig();

            log.info("Timeout 테스트 - timeoutDuration: " + config.getTimeoutDuration().toMillis() + "ms");

            // When & Then - timeout 설정값을 초과하는 작업 실행 (헬퍼 메서드 사용)
            long sleepTime = config.getTimeoutDuration().toMillis() + 10;

            // Then - TimeoutException이 발생해야 함
            assertThatThrownBy(() -> {
                executeTimeLimiterTask(timeLimiter, sleepTime);
            }).hasCauseInstanceOf(java.util.concurrent.TimeoutException.class);

            log.info("Timeout 발생 확인 완료");
        }

        @Test
        @DisplayName("PG_FIND_TL TimeLimiter가 timeout 설정값을 초과하면 TimeoutException이 발생해야 한다")
        public void shouldThrowTimeoutExceptionForPgRequest() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_REQUEST_TL);
            var config = timeLimiter.getTimeLimiterConfig();

            log.info("Timeout 테스트 - timeoutDuration: " + config.getTimeoutDuration().toMillis() + "ms");

            // When & Then - timeout 설정값을 초과하는 작업 실행 (헬퍼 메서드 사용)
            long sleepTime = config.getTimeoutDuration().toMillis() + 10;

            // Then - TimeoutException이 발생해야 함
            assertThatThrownBy(() -> {
                executeTimeLimiterTask(timeLimiter, sleepTime);
            }).hasCauseInstanceOf(java.util.concurrent.TimeoutException.class);

            log.info("Timeout 발생 확인 완료");
        }
    }

    @DisplayName("PG_REQUEST_TL")
    @Nested
    class PgRequestTl {
        @Test
        @DisplayName("실제 등록된 PG_REQUEST_TL TimeLimiter의 설정을 확인해야 한다")
        public void shouldCheckActualPgRequestTimeLimiterConfiguration() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_REQUEST_TL);
            var config = timeLimiter.getTimeLimiterConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_REQUEST_TL 실제 설정 ===");
            log.info("timeoutDuration: {}ms", config.getTimeoutDuration().toMillis());
            log.info("cancelRunningFuture: {}", config.shouldCancelRunningFuture());
            log.info("=============================");

            // 설정값 검증
            assertThat(config).isNotNull();
            assertThat(config.getTimeoutDuration().toMillis()).isGreaterThan(0);
        }
    }
}
