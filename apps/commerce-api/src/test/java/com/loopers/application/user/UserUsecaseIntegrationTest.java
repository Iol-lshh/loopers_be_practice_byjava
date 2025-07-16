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
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );

            // act
            var result = userFacade.signUp(command);

            // verify
            verify(userRepository).exists(any(UserCriteria.class));
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
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            userFacade.signUp(commandAlreadyExists);
            var commandUtd = new UserCommand.Create(
                    "testUser",
                    Gender.FEMALE,
                    "1993-04-09",
                    "test@naver.com"
            );

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                userFacade.signUp(commandUtd);
            });

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
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );
            UserEntity expected = UserEntity.of(command);
            UserEntity savedUser = userRepository.save(expected);

            // act
            UserInfo result = userFacade.get("testUser");

            // assert
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals(expected.getLoginId(), result.loginId());
            assertEquals(expected.getGender().getValue(), result.gender());
            assertEquals(expected.getBirthDate(), result.birthDate());
            assertEquals(expected.getEmail(), result.email());
        }

        @Test
        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        void returnsNull_whenUserDoesNotExist() {
            // arrange
            String nonExistentLoginId = "nonExistentUser";

            // act
            UserInfo result = userFacade.get(nonExistentLoginId);

            // assert
            assertNull(result);
        }
    }
}
