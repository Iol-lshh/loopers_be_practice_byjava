package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PointModelTest {

    @DisplayName("포인트 충전")
    @Nested
    class Charge{
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @Test
        void failsToChargePoints_whenAmountIsZeroOrNegative() {
            // arrange
            PointEntity point = PointEntity.init(1L);
            long invalidAmount = -100;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> point.add(invalidAmount));

            // assert
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("0 초과로 포인트가 충전시 성공한다.")
        @ParameterizedTest
        @ValueSource(longs = {1, 100, 1000000000})
        void succeedsToChargePoints_whenAmountIsPositive(long validAmount) {
            // arrange
            PointEntity point = PointEntity.init(1L);

            // act
            point.add(validAmount);

            // assert
            assertEquals(validAmount, point.getAmount());
        }

        @DisplayName("0이하로 포인트를 충전시 BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        void failsToChargePoints_whenAmountIsZero(long invalidAmount) {
            // arrange
            PointEntity point = PointEntity.init(1L);

            // act
            var result = assertThrows(CoreException.class, () -> point.add(invalidAmount));

            // assert
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }

        @DisplayName("long 범위 초과한 포인트를 충전시 BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {
                Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 100
        })
        void failsToChargePoints_whenAmountIsZero(Long overflowAmount) {
            // arrange
            PointEntity point = PointEntity.init(1L);
            point.add(1000L);
            assertEquals(1000L, point.getAmount());

            // act
            var result = assertThrows(CoreException.class, () -> point.add(overflowAmount));

            // assert
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }
    }

    @DisplayName("포인트 차감")
    @Nested
    class Subtract {
        @DisplayName("0 이하의 정수로 포인트를 차감 시 실패한다.")
        @Test
        void failsToSubtractPoints_whenAmountIsZeroOrNegative() {
            // arrange
            PointEntity point = PointEntity.init(1L);
            long invalidAmount = -100;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> point.subtract(invalidAmount));

            // assert
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("0 초과로 포인트가 차감시 성공한다.")
        @ParameterizedTest
        @ValueSource(longs = {1, 100, 1000000000})
        void succeedsToSubtractPoints_whenAmountIsPositive(long validAmount) {
            // arrange
            Long origin = 1000000000L;
            PointEntity point = PointEntity.init(1L);
            point.add(origin);

            // act
            point.subtract(validAmount);

            // assert
            assertEquals(origin - validAmount, point.getAmount());
        }

        @DisplayName("0이하로 포인트를 차감시 BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        void failsToSubtractPoints_whenAmountIsZero(long invalidAmount) {
            // arrange
            PointEntity point = PointEntity.init(1L);
            point.add(1000L);

            // act
            var result = assertThrows(CoreException.class, () -> point.subtract(invalidAmount));

            // assert
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }

        @DisplayName("차감할 포인트가 현재 보유 포인트보다 많을 경우 BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToSubtractPoints_whenInsufficientBalance() {
            // arrange
            PointEntity point = PointEntity.init(1L);
            point.add(500L);

            // act
            var result = assertThrows(CoreException.class, () -> point.subtract(600L));

            // assert
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }
    }

}
