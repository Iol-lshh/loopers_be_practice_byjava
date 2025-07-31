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

import java.util.Optional;

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
}
