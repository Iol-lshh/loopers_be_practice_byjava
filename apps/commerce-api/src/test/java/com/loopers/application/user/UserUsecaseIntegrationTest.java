package com.loopers.application.user;

import com.loopers.domain.user.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserUsecaseIntegrationTest {

    @MockitoSpyBean
    private UserRepository userRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private UserService userService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("회원가입")
    class register {
        @Test
        @DisplayName("회원 가입시 User 저장이 수행된다. ( spy 검증 )")
        void saveUser_whenUserRegisters() {
            var command = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );

            // act
            var result = userFacade.signUp(command);

            // verify
            verify(userRepository).exists(any(UserStatement.class));
            verify(userRepository).save(any(UserEntity.class));
            assertNotNull(result);
            assertEquals("testUser", result.loginId());
        }

        @Test
        @DisplayName("이미 가입된 ID로 회원가입 시도 시, 실패한다.")
        void failToRegister_whenLoginIdAlreadyExists() {
            // arrange
            var commandAlreadyExists = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            userFacade.signUp(commandAlreadyExists);
            var commandUtd = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.FEMALE,
                    "1993-04-09",
                    "test@naver.com"
            );

            // act
            CoreException exception = assertThrows(CoreException.class, () -> userFacade.signUp(commandUtd));

            // assert
            assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class getMyInfo {
        @Test
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        void returnsUserInfo_whenUserExists() {
            // arrange
            var command = new UserCommand.Create(
                    "testUser",
                    UserEntity.Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            UserResult expected = userFacade.signUp(command);

            // act
            UserResult result = userFacade.get(expected.id());

            // assert
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, 빈 Optional이 반환된다.")
        void returnsEmptyOptional_whenUserDoesNotExist() {
            // arrange
            Long nonExistentUserId = 999L;

            // act
            Optional<UserEntity> result = userService.find(nonExistentUserId);

            // assert
            assertTrue(result.isEmpty());
        }
    }
}
