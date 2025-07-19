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
            var user = UserEntity.of(UserCommand.Create.of(
                    "testUser", "남",
                    "1993-04-09", "test@gmail.com"
                    ));
            long invalidAmount = -100;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                user.charge(invalidAmount);
            });

            // assert
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("0 이상으로 포인트가 충전시 성공한다.")
        @Test
        void succeedsToChargePoints_whenAmountIsPositive() {
            // arrange
            var user = UserEntity.of(UserCommand.Create.of(
                    "testUser", "남",
                    "1993-04-09", "test@gmail.com"
            ));
            long validAmount = 100;

            // act
            user.charge(validAmount);
            var currentPoint = user.getPoint();

            // assert
            assertNotNull(currentPoint);
            assertEquals(100, currentPoint.getAmount());
        }
    }
}
