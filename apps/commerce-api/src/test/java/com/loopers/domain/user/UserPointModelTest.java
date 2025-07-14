package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPointModelTest {

    @Nested
    class Charge{
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @Test
        void failsToChargePoints_whenAmountIsZeroOrNegative() {
            // arrange
            long testUserId = 1L;
            var utd = UserPointVo.init();
            long invalidAmount = -100;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                utd.charge(invalidAmount);
            });

            // assert
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }
}
