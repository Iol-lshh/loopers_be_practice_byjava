package com.loopers.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ResilienceTest extends AbstractResilienceTest{

    @Test
    @DisplayName("실패를 여러 번 발생시켜, testCircuitBreaker가 OPEN 상태가 되어야 한다")
    public void shouldOpenPgRequestCircuitBreakerByRepeatedFailures() {
        // Given - 테스트용 민감한 설정으로 서킷브레이커 생성
        CircuitBreaker circuitBreaker = CircuitBreaker.of("testCircuitBreaker",
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .minimumNumberOfCalls(2)
                        .slidingWindowSize(4)
                        .build());

        // When - 실패를 여러 번 발생시켜 서킷브레이커 열기
        IntStream.rangeClosed(1, 4).forEach(count -> {
            try {
                produceFailure(circuitBreaker);
            } catch (Exception e) {
                // 예외는 무시하고 계속 호출
            }
        });

        // Then - 서킷브레이커가 OPEN 상태가 되어야 함
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("HALF_OPEN 상태에서 성공을 여러 번 발생시켜, testCircuitBreaker가 CLOSED 상태가 되어야 한다")
    public void shouldClosePgFindCircuitBreakerByRepeatedSuccess() {
        // Given - 테스트용 설정으로 서킷브레이커 생성
        CircuitBreaker circuitBreaker = CircuitBreaker.of("testCircuitBreaker",
                io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .minimumNumberOfCalls(2)
                        .slidingWindowSize(4)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .build());

        // 먼저 실패를 발생시켜 OPEN 상태로 만들기
        IntStream.rangeClosed(1, 4).forEach(count -> {
            try {
                produceFailure(circuitBreaker);
            } catch (Exception e) {
                // 예외는 무시
            }
        });

        // HALF_OPEN 상태로 전환
        circuitBreaker.transitionToHalfOpenState();

        // When - 성공을 여러 번 발생시켜 서킷브레이커 닫기
        IntStream.rangeClosed(1, 3).forEach(count -> {
            produceSuccess(circuitBreaker);
        });

        // Then - 서킷브레이커가 CLOSED 상태가 되어야 함
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
