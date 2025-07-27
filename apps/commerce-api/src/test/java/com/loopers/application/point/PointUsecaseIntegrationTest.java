package com.loopers.application.point;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointUsecaseIntegrationTest {

    @Autowired
    private PointFacade pointFacade;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UserService userService;

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
            PointInfo points = pointFacade.get(tester.getLoginId());

            // then
            assertNotNull(points);
            assertNotNull(points.point());
            assertEquals(tester.getId(), points.userId());
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, CoreException:NOT_FOUND 가 발생한다.")
        @Test
        void throwNotFound_whenUserDoesNotExist() {
            // given
            String loginId = "nonExistentUser";

            // when
            CoreException coreException = assertThrows(CoreException.class,
                    () -> pointFacade.get(loginId));

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
            Long chargeAmount = 100L;
            pointFacade.charge(tester.getLoginId(), chargeAmount);

            // when
            PointInfo result = pointFacade.get(tester.getLoginId());

            // then
            assertNotNull(result);
            assertNotNull(result.point());
            assertEquals(tester.getId(), result.userId());
            assertEquals(chargeAmount, result.point());
        }


        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void throwNotFound_whenUserDoesNotExist() {
            // given
            String loginId = "nonExistentUser";

            // when
            CoreException coreException = assertThrows(CoreException.class,
                    () -> pointFacade.charge(loginId, 100L));

            // then
            assertEquals(ErrorType.NOT_FOUND, coreException.getErrorType());
        }
    }
}
