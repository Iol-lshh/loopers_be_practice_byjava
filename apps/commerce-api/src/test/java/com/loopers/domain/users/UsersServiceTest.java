package com.loopers.domain.users;

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
import static org.mockito.Mockito.*;

@SpringBootTest
class UsersServiceTest {

    @Autowired
    private UsersService usersService;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    
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
            var spyUsersRepository = spy(usersRepository);
            UsersService spyUsersService = new UsersService(spyUsersRepository);

            // act
            UsersModel result = spyUsersService.register("testUser",
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com");

            // verify
            verify(spyUsersRepository).existsByLoginId("testUser");
            verify(spyUsersRepository).save(any(UsersModel.class));
            assertNotNull(result);
            assertEquals("testUser", result.getLoginId());
        }

        @Test
        @DisplayName("이미 가입된 ID로 회원가입 시도 시, 실패한다.")
        void failToRegister_whenLoginIdAlreadyExists() {
            // arrange
            usersService.register("testUser",
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com");

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                usersService.register("testUser",
                        Gender.FEMALE,
                        "1993-04-09",
                        "test@naver.com");
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
            UsersModel expected = UsersModel.of("testUser",
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com");
            UsersModel savedUser = usersRepository.save(expected);
            String loginId = savedUser.getLoginId();

            // act
            UsersModel result = usersService.getMyInfo(loginId);

            // assert
            assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.getId()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNotNull(result.getUpdatedAt()),
                () -> assertEquals(expected.getLoginId(), result.getLoginId()),
                () -> assertEquals(expected.getGender(), result.getGender()),
                () -> assertEquals(expected.getBirthDate(), result.getBirthDate()),
                () -> assertEquals(expected.getEmail(), result.getEmail())
            );
        }

        @Test
        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        void returnsNull_whenUserDoesNotExist() {
            // arrange
            String nonExistentLoginId = "nonExistentUser";

            // act
            UsersModel result = usersService.getMyInfo(nonExistentLoginId);

            // assert
            assertNull(result);
        }
    }
}
