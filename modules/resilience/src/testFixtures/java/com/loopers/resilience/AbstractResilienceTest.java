package com.loopers.resilience;

import com.loopers.config.resilience.ResilienceConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ResilienceConfig.class})
public abstract class AbstractResilienceTest {

    @Autowired
    protected CircuitBreakerRegistry circuitBreakerRegistry;

    protected void transitionToOpenState(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        circuitBreaker.transitionToOpenState();
    }

    protected void transitionToClosedState(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        circuitBreaker.transitionToClosedState();
    }

    protected void checkHealthStatus(String circuitBreakerName, CircuitBreaker.State state) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        assertThat(circuitBreaker.getState()).isEqualTo(state);
    }

    protected void produceFailure(CircuitBreaker circuitBreaker) {
        circuitBreaker.executeSupplier(() -> {
            throw new RuntimeException("Simulated failure");
        });
    }

    protected void produceSuccess(CircuitBreaker circuitBreaker) {
        circuitBreaker.executeSupplier(() -> "success");
    }

    protected int calculateRequiredFailures(CircuitBreakerConfig config) {
        // 실패율 임계값에 도달하기 위해 필요한 최소 실패 횟수 계산
        double failureRate = config.getFailureRateThreshold() / 100.0;
        int minCalls = config.getMinimumNumberOfCalls();
        int windowSize = config.getSlidingWindowSize();

        // 보수적으로 더 많은 실패를 발생시키기 위해 계산
        int requiredFailures = Math.max(minCalls, (int) Math.ceil(windowSize * failureRate));
        return Math.min(requiredFailures + 2, windowSize); // 여유분 추가
    }

    /*
    * --------------------------------------------------------------------
    * */
    @Autowired
    protected TimeLimiterRegistry timeLimiterRegistry;

    protected String executeTimeLimiterTask(TimeLimiter timeLimiter, long durationMs) throws Exception {
        System.out.println("executeTimeLimiterTask 시작 - durationMs: " + durationMs);
        System.out.println("TimeLimiter 설정 - timeoutDuration: " + timeLimiter.getTimeLimiterConfig().getTimeoutDuration().toMillis() + "ms");
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            java.util.concurrent.CompletableFuture<String> future = timeLimiter.executeCompletionStage(executor, () -> 
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("Thread.sleep 시작 - " + durationMs + "ms");
                        Thread.sleep(durationMs);
                        System.out.println("Thread.sleep 완료");
                        return "success";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                })
            ).toCompletableFuture();
            
            String result = future.join(); // join()에서 timeout이 발생해야 함
            System.out.println("executeTimeLimiterTask 완료 - 결과: " + result);
            return result;
        } finally {
            executor.shutdown();
        }
    }

    /*
    * --------------------------------------------------------------------
    * */
   @Autowired
    protected RetryRegistry retryRegistry;
    protected static final String FAILED_WITH_RETRY = "failed_with_retry";
    protected static final String SUCCESS_WITHOUT_RETRY = "successful_without_retry";

    protected float getCurrentCount(String kind, String backend) {
        Retry.Metrics metrics = retryRegistry.retry(backend).getMetrics();

        if (FAILED_WITH_RETRY.equals(kind)) {
            return metrics.getNumberOfFailedCallsWithRetryAttempt();
        }
        if (SUCCESS_WITHOUT_RETRY.equals(kind)) {
            return metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt();
        }

        return 0;
    }
}
