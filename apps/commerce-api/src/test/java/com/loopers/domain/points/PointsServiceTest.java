package com.loopers.domain.points;

import com.loopers.domain.users.UsersModel;
import com.loopers.domain.users.UsersService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.type.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointsServiceTest {

    @Autowired
    private PointsService pointsService;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UsersService usersService;

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
            String loginId = "testUser";
            UsersModel tester = usersService.register(
                    loginId,
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );

            // when
            PointsModel points = pointsService.get(loginId);

            // then
            assertNotNull(points);
            assertNotNull(points.getId());
            assertNotNull(points.getAmount());
            assertEquals(tester.getId(), points.getUserId());
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void getPoints_whenUserDoesNotExist() {
            // given
            String loginId = "nonExistentUser";

            // when
            PointsModel points = pointsService.get(loginId);

            // then
            assertNull(points);
        }
    }

    @DisplayName("포인트 충전")
    @Nested
    class Charge {

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void chargePoints_whenUserExists() {
            // given
            String loginId = "nonExistentUser";

            // when
            CoreException coreException = assertThrows(CoreException.class,
                    () -> pointsService.charge(loginId, 100L));

            // then
            assertEquals(ErrorType.NOT_FOUND, coreException.getErrorType());
        }
    }
}
