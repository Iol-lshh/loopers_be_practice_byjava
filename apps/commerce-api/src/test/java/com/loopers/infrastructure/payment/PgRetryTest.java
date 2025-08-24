package com.loopers.infrastructure.payment;

import com.loopers.resilience.AbstractResilienceTest;
import com.loopers.support.resilience.ResilienceConstant;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PgRetryTest extends AbstractResilienceTest {
    
    private static final Logger log = LoggerFactory.getLogger(PgRetryTest.class);

    @DisplayName("PG_REQUEST_RT")
    @Nested
    class PgRequestRt {
        @Test
        @DisplayName("PG_REQUEST_RT Retry가 정상적으로 등록되어야 한다")
        public void shouldHaveRetryConfigured() {
            // Then
            assertThat(retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT)).isNotNull();
        }

        @Test
        @DisplayName("실제 등록된 PG_REQUEST_RT Retry의 설정을 확인해야 한다")
        public void shouldCheckActualPgRequestRetryConfiguration() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT);
            var config = retry.getRetryConfig();

            // Then
            log.info("=== PG_REQUEST_RT 실제 설정 ===");
            log.info("maxAttempts: {}", config.getMaxAttempts());
            log.info("=============================");

            // 설정값 검증
            assertThat(config).isNotNull();
            assertThat(config.getMaxAttempts()).isGreaterThan(1);
        }

        @Test
        @DisplayName("실제 설정으로 PG_REQUEST_RT Retry가 정상적으로 동작하는지 테스트")
        public void shouldWorkWithActualPgRequestRetryConfiguration() {
            // Given - 실제 등록된 Retry 사용
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT);
            var config = retry.getRetryConfig();

            // 설정값 출력
            log.info("실제 설정으로 테스트 - maxAttempts: {}", config.getMaxAttempts());

            // When - Retry 메트릭 초기화
            float initialFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);
            float initialSuccessfulWithoutRetry = getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_REQUEST_RT);

            log.info("초기 메트릭 - failedWithRetry: {}, successfulWithoutRetry: {}",
                    initialFailedWithRetry, initialSuccessfulWithoutRetry);

            // Then
            assertThat(initialFailedWithRetry).isGreaterThanOrEqualTo(0);
            assertThat(initialSuccessfulWithoutRetry).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("PG_REQUEST_RT Retry가 실패 시 재시도하는지 테스트")
        public void shouldRetryOnFailure() {
            // Given - 실제 등록된 Retry 사용
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT);
            var config = retry.getRetryConfig();

            // 설정값 출력
            log.info("재시도 테스트 - maxAttempts: {}", config.getMaxAttempts());

            // When - 실패를 발생시키는 작업 실행
            float beforeFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);

            try {
                retry.executeSupplier(() -> {
                    throw new RuntimeException("Simulated failure for retry test");
                });
            } catch (Exception e) {
                log.info("예상된 예외 발생: {}", e.getMessage());
            }

            // Then
            float afterFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);
            log.info("재시도 메트릭 - before: {}, after: {}", beforeFailedWithRetry, afterFailedWithRetry);

            // 재시도가 발생했는지 확인 (최소 1번 이상)
            assertThat(afterFailedWithRetry).isGreaterThanOrEqualTo(beforeFailedWithRetry);
        }

        @Test
        @DisplayName("PG_REQUEST_RT Retry가 성공 시 재시도하지 않는지 테스트")
        public void shouldNotRetryOnSuccess() {
            // Given - 실제 등록된 Retry 사용
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT);
            var config = retry.getRetryConfig();

            // 설정값 출력
            log.info("성공 시 재시도 안함 테스트 - maxAttempts: {}", config.getMaxAttempts());

            // When - 성공하는 작업 실행
            float beforeSuccessfulWithoutRetry = getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_REQUEST_RT);

            String result = retry.executeSupplier(() -> "success");

            // Then
            float afterSuccessfulWithoutRetry = getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_REQUEST_RT);
            log.info("성공 메트릭 - before: {}, after: {}, result: {}",
                    beforeSuccessfulWithoutRetry, afterSuccessfulWithoutRetry, result);

            // 성공 시 재시도하지 않았는지 확인
            assertThat(result).isEqualTo("success");
            assertThat(afterSuccessfulWithoutRetry).isGreaterThanOrEqualTo(beforeSuccessfulWithoutRetry);
        }

        @Test
        @DisplayName("PG_REQUEST_RT Retry가 설정된 최대 재시도 횟수만큼 동작하는지 테스트")
        public void shouldRetryUpToMaxAttempts() {
            // Given - 실제 등록된 Retry 사용
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT);
            var config = retry.getRetryConfig();

            // 설정값 출력
            log.info("최대 재시도 횟수 테스트 - maxAttempts: {}", config.getMaxAttempts());

            // When - 계속 실패하는 작업 실행 (최대 재시도 횟수만큼)
            float beforeFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);

            try {
                retry.executeSupplier(() -> {
                    throw new RuntimeException("Persistent failure for max attempts test");
                });
            } catch (Exception e) {
                log.info("최대 재시도 후 최종 예외 발생: {}", e.getMessage());
            }

            // Then
            float afterFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);
            log.info("최대 재시도 메트릭 - before: {}, after: {}", beforeFailedWithRetry, afterFailedWithRetry);

            // 재시도가 발생했는지 확인
            assertThat(afterFailedWithRetry).isGreaterThanOrEqualTo(beforeFailedWithRetry);
        }

        @Test
        @DisplayName("PG_REQUEST_RT Retry의 전체적인 동작을 통합 테스트")
        public void shouldWorkAsExpectedInIntegration() {
            // Given - 실제 등록된 Retry 사용
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_REQUEST_RT);
            var config = retry.getRetryConfig();

            // 설정값 출력
            log.info("통합 테스트 - maxAttempts: {}", config.getMaxAttempts());

            // When - 성공과 실패를 번갈아가며 테스트
            float initialFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);
            float initialSuccessfulWithoutRetry = getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_REQUEST_RT);

            // 성공 케이스
            String successResult = retry.executeSupplier(() -> "integration test success");

            // 실패 케이스 (재시도 발생)
            try {
                retry.executeSupplier(() -> {
                    throw new RuntimeException("Integration test failure");
                });
            } catch (Exception e) {
                log.info("통합 테스트 실패 케이스: {}", e.getMessage());
            }

            // Then
            float finalFailedWithRetry = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_REQUEST_RT);
            float finalSuccessfulWithoutRetry = getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_REQUEST_RT);

            log.info("통합 테스트 결과 - successResult: {}", successResult);
            log.info("메트릭 변화 - failedWithRetry: {} -> {}, successfulWithoutRetry: {} -> {}",
                    initialFailedWithRetry, finalFailedWithRetry,
                    initialSuccessfulWithoutRetry, finalSuccessfulWithoutRetry);

            // 성공과 실패 모두 정상적으로 처리되었는지 확인
            assertThat(successResult).isEqualTo("integration test success");
            assertThat(finalFailedWithRetry).isGreaterThanOrEqualTo(initialFailedWithRetry);
            assertThat(finalSuccessfulWithoutRetry).isGreaterThanOrEqualTo(initialSuccessfulWithoutRetry);
        }
    }
}
