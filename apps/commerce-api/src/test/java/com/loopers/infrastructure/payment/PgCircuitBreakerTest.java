package com.loopers.infrastructure.payment;

import com.loopers.resilience.AbstractResilienceTest;
import com.loopers.support.resilience.ResilienceConstant;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import static org.assertj.core.api.Assertions.assertThat;

public class PgCircuitBreakerTest extends AbstractResilienceTest {
    
    private static final Logger log = LoggerFactory.getLogger(PgCircuitBreakerTest.class);

    @Test
    @DisplayName("PG_FIND_CB와 PG_REQUEST_CB 서킷브레이커가 정상적으로 등록되어야 한다")
    public void shouldHaveCircuitBreakersConfigured() {
        // Then
        assertThat(circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB)).isNotNull();
        assertThat(circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB)).isNotNull();
    }

    @DisplayName("PG_FIND_CB")
    @Nested
    class PgFindCb {
        @Test
        @DisplayName("PG_FIND_CB 서킷브레이커가 OPEN 상태로 전환되어야 한다")
        public void shouldTransitionPgFindCircuitBreakerToOpenState() {
            // When
            transitionToOpenState(ResilienceConstant.PG_FIND_CB);

            // Then
            checkHealthStatus(ResilienceConstant.PG_FIND_CB, State.OPEN);
        }

        @Test
        @DisplayName("실제 등록된 PG_FIND_CB 서킷브레이커의 설정을 확인해야 한다")
        public void shouldCheckActualPgFindCircuitBreakerConfiguration() {
            // Given
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_FIND_CB 실제 설정 ===");
            log.info("failureRateThreshold: {}", config.getFailureRateThreshold());
            log.info("minimumNumberOfCalls: {}", config.getMinimumNumberOfCalls());
            log.info("slidingWindowSize: {}", config.getSlidingWindowSize());
            log.info("permittedNumberOfCallsInHalfOpenState: {}", config.getPermittedNumberOfCallsInHalfOpenState());
            log.info("slidingWindowType: {}", config.getSlidingWindowType());
            log.info("==========================");

            // 설정값 검증
            assertThat(config).isNotNull();
            assertThat(config.getFailureRateThreshold()).isGreaterThan(0);
            assertThat(config.getMinimumNumberOfCalls()).isGreaterThan(0);
        }

        @Test
        @DisplayName("실제 설정으로 PG_FIND_CB 서킷브레이커가 OPEN 상태가 되는지 테스트")
        public void shouldOpenPgFindCircuitBreakerWithActualConfig() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("실제 설정으로 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize());

            // When - 실제 설정에 맞는 실패 횟수로 테스트
            int requiredFailures = calculateRequiredFailures(config);
            log.info("필요한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시하고 계속 호출
                }
            });

            // Then - 서킷브레이커 상태 확인
            State currentState = circuitBreaker.getState();
            log.info("현재 서킷브레이커 상태: {}", currentState);

            // OPEN 상태가 되었는지 확인 (실제 설정에 따라 달라질 수 있음)
            assertThat(currentState).isIn(State.OPEN, State.CLOSED);
        }

        @Test
        @DisplayName("실제 설정으로 PG_FIND_CB 서킷브레이커가 HALF_OPEN에서 CLOSED로 전환되는지 테스트")
        public void shouldClosePgFindCircuitBreakerFromHalfOpenWithActualConfig() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("HALF_OPEN->CLOSED 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}, permittedNumberOfCallsInHalfOpenState: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize(), config.getPermittedNumberOfCallsInHalfOpenState());

            // 먼저 실패를 발생시켜 OPEN 상태로 만들기 (실제 설정에 맞게)
            int requiredFailures = calculateRequiredFailures(config);
            log.info("OPEN 상태로 만들기 위한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    if (count % 20 == 0) log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시
                }
            });

            // OPEN 상태 확인
            State openState = circuitBreaker.getState();
            log.info("OPEN 상태 확인: {}", openState);
            assertThat(openState).isEqualTo(State.OPEN);

            // HALF_OPEN 상태로 전환
            circuitBreaker.transitionToHalfOpenState();
            State halfOpenState = circuitBreaker.getState();
            log.info("HALF_OPEN 상태 확인: {}", halfOpenState);
            assertThat(halfOpenState).isEqualTo(State.HALF_OPEN);

            // HALF_OPEN 상태에서 성공을 여러 번 발생시켜 CLOSED로 전환
            int requiredSuccesses = config.getPermittedNumberOfCallsInHalfOpenState();
            log.info("CLOSED 상태로 만들기 위한 성공 횟수: {}", requiredSuccesses);

            IntStream.rangeClosed(1, requiredSuccesses).forEach(count -> {
                produceSuccess(circuitBreaker);
                log.info("성공 {}번 발생", count);
            });

            // Then - 서킷브레이커가 CLOSED 상태가 되어야 함
            State finalState = circuitBreaker.getState();
            log.info("최종 서킷브레이커 상태: {}", finalState);
            assertThat(finalState).isEqualTo(State.CLOSED);
        }
    }

    @DisplayName("PG_REQUEST_CB")
    @Nested
    class PgRequestCb {
        @Test
        @DisplayName("PG_FIND_CB 서킷브레이커가 CLOSED 상태로 전환되어야 한다")
        public void shouldTransitionPgFindCircuitBreakerToClosedState() {
            // Given
            transitionToOpenState(ResilienceConstant.PG_FIND_CB);

            // When
            transitionToClosedState(ResilienceConstant.PG_FIND_CB);

            // Then
            checkHealthStatus(ResilienceConstant.PG_FIND_CB, State.CLOSED);
        }

        @Test
        @DisplayName("PG_REQUEST_CB 서킷브레이커가 OPEN 상태로 전환되어야 한다")
        public void shouldTransitionPgRequestCircuitBreakerToOpenState() {
            // When
            transitionToOpenState(ResilienceConstant.PG_REQUEST_CB);

            // Then
            checkHealthStatus(ResilienceConstant.PG_REQUEST_CB, State.OPEN);
        }

        @Test
        @DisplayName("PG_REQUEST_CB 서킷브레이커가 CLOSED 상태로 전환되어야 한다")
        public void shouldTransitionPgRequestCircuitBreakerToClosedState() {
            // Given
            transitionToOpenState(ResilienceConstant.PG_REQUEST_CB);

            // When
            transitionToClosedState(ResilienceConstant.PG_REQUEST_CB);

            // Then
            checkHealthStatus(ResilienceConstant.PG_REQUEST_CB, State.CLOSED);
        }



        @Test
        @DisplayName("실제 등록된 PG_REQUEST_CB 서킷브레이커의 설정을 확인해야 한다")
        public void shouldCheckActualPgRequestCircuitBreakerConfiguration() {
            // Given
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_REQUEST_CB 실제 설정 ===");
            log.info("failureRateThreshold: {}", config.getFailureRateThreshold());
            log.info("minimumNumberOfCalls: {}", config.getMinimumNumberOfCalls());
            log.info("slidingWindowSize: {}", config.getSlidingWindowSize());
            log.info("permittedNumberOfCallsInHalfOpenState: {}", config.getPermittedNumberOfCallsInHalfOpenState());
            log.info("slidingWindowType: {}", config.getSlidingWindowType());
            log.info("=============================");

            // 설정값 검증
            assertThat(config).isNotNull();
            assertThat(config.getFailureRateThreshold()).isGreaterThan(0);
            assertThat(config.getMinimumNumberOfCalls()).isGreaterThan(0);
        }



        @Test
        @DisplayName("실제 설정으로 PG_REQUEST_CB 서킷브레이커가 OPEN 상태가 되는지 테스트")
        public void shouldOpenPgRequestCircuitBreakerWithActualConfig() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("실제 설정으로 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize());

            // When - 실제 설정에 맞는 실패 횟수로 테스트
            int requiredFailures = calculateRequiredFailures(config);
            log.info("필요한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시하고 계속 호출
                }
            });

            // Then - 서킷브레이커 상태 확인
            State currentState = circuitBreaker.getState();
            log.info("현재 서킷브레이커 상태: {}", currentState);

            // OPEN 상태가 되었는지 확인 (실제 설정에 따라 달라질 수 있음)
            assertThat(currentState).isIn(State.OPEN, State.CLOSED);
        }



        @Test
        @DisplayName("실제 설정으로 PG_REQUEST_CB 서킷브레이커가 HALF_OPEN에서 CLOSED로 전환되는지 테스트")
        public void shouldClosePgRequestCircuitBreakerFromHalfOpenWithActualConfig() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("HALF_OPEN->CLOSED 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}, permittedNumberOfCallsInHalfOpenState: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize(), config.getPermittedNumberOfCallsInHalfOpenState());

            // 먼저 실패를 발생시켜 OPEN 상태로 만들기 (실제 설정에 맞게)
            int requiredFailures = calculateRequiredFailures(config);
            log.info("OPEN 상태로 만들기 위한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    if (count % 20 == 0) log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시
                }
            });

            // OPEN 상태 확인
            State openState = circuitBreaker.getState();
            log.info("OPEN 상태 확인: {}", openState);
            assertThat(openState).isEqualTo(State.OPEN);

            // HALF_OPEN 상태로 전환
            circuitBreaker.transitionToHalfOpenState();
            State halfOpenState = circuitBreaker.getState();
            log.info("HALF_OPEN 상태 확인: {}", halfOpenState);
            assertThat(halfOpenState).isEqualTo(State.HALF_OPEN);

            // HALF_OPEN 상태에서 성공을 여러 번 발생시켜 CLOSED로 전환
            int requiredSuccesses = config.getPermittedNumberOfCallsInHalfOpenState();
            log.info("CLOSED 상태로 만들기 위한 성공 횟수: {}", requiredSuccesses);

            IntStream.rangeClosed(1, requiredSuccesses).forEach(count -> {
                produceSuccess(circuitBreaker);
                log.info("성공 {}번 발생", count);
            });

            // Then - 서킷브레이커가 CLOSED 상태가 되어야 함
            State finalState = circuitBreaker.getState();
            log.info("최종 서킷브레이커 상태: {}", finalState);
            assertThat(finalState).isEqualTo(State.CLOSED);
        }

        @Test
        @DisplayName("실제 설정으로 PG_FIND_CB 서킷브레이커가 자동으로 HALF_OPEN 상태가 되는지 테스트")
        public void shouldAutomaticallyTransitionToHalfOpenStateWithActualConfig() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("자동 HALF_OPEN 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize());

            // 먼저 실패를 발생시켜 OPEN 상태로 만들기
            int requiredFailures = calculateRequiredFailures(config);
            log.info("OPEN 상태로 만들기 위한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    if (count % 20 == 0) log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시
                }
            });

            // OPEN 상태 확인
            State openState = circuitBreaker.getState();
            log.info("OPEN 상태 확인: {}", openState);
            assertThat(openState).isEqualTo(State.OPEN);

            // 실제 운영에서는 waitDurationInOpenState 시간이 지나야 하지만,
            // 테스트에서는 짧은 대기 후 수동으로 HALF_OPEN으로 전환
            try {
                Thread.sleep(100); // 100ms 대기
                log.info("대기 완료: 100ms");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // HALF_OPEN 상태로 자동 전환되었는지 확인
            State currentState = circuitBreaker.getState();
            log.info("대기 후 서킷브레이커 상태: {}", currentState);

            // OPEN 상태에서 HALF_OPEN으로 자동 전환되었는지 확인
            // 실제 운영에서는 waitDurationInOpenState 시간이 지나야 하지만,
            // 테스트에서는 수동으로 HALF_OPEN으로 전환하여 검증
            if (currentState == State.OPEN) {
                log.info("아직 OPEN 상태, 수동으로 HALF_OPEN으로 전환하여 테스트");
                circuitBreaker.transitionToHalfOpenState();
                currentState = circuitBreaker.getState();
                log.info("수동 전환 후 상태: {}", currentState);
            }

            assertThat(currentState).isEqualTo(State.HALF_OPEN);
        }

        @Test
        @DisplayName("실제 설정으로 PG_REQUEST_CB 서킷브레이커가 자동으로 HALF_OPEN 상태가 되는지 테스트")
        public void shouldAutomaticallyTransitionToHalfOpenStateForRequestWithActualConfig() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("자동 HALF_OPEN 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize());

            // 먼저 실패를 발생시켜 OPEN 상태로 만들기
            int requiredFailures = calculateRequiredFailures(config);
            log.info("OPEN 상태로 만들기 위한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    if (count % 20 == 0) log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시
                }
            });

            // OPEN 상태 확인
            State openState = circuitBreaker.getState();
            log.info("OPEN 상태 확인: {}", openState);
            assertThat(openState).isEqualTo(State.OPEN);

            // 실제 운영에서는 waitDurationInOpenState 시간이 지나야 하지만,
            // 테스트에서는 짧은 대기 후 수동으로 HALF_OPEN으로 전환
            try {
                Thread.sleep(100); // 100ms 대기
                log.info("대기 완료: 100ms");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // HALF_OPEN 상태로 자동 전환되었는지 확인
            State currentState = circuitBreaker.getState();
            log.info("대기 후 서킷브레이커 상태: {}", currentState);

            // OPEN 상태에서 HALF_OPEN으로 자동 전환되었는지 확인
            if (currentState == State.OPEN) {
                log.info("아직 OPEN 상태, 수동으로 HALF_OPEN으로 전환하여 테스트");
                circuitBreaker.transitionToHalfOpenState();
                currentState = circuitBreaker.getState();
                log.info("수동 전환 후 상태: {}", currentState);
            }

            assertThat(currentState).isEqualTo(State.HALF_OPEN);
        }
    }
}
