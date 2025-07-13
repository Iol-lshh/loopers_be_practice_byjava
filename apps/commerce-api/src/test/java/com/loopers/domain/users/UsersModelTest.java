package com.loopers.domain.users;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.type.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UsersModelTest {
    @Nested
    class Create {
        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void failsToCreateUser_whenLoginIdIsInvalid() {
            // arrange
            String invalidLoginId = "invalid_login_id";

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UsersModel.of(invalidLoginId,
                        Gender.MALE,
                        "1993-04-09",
                        "test@gmail.com");
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void failsToCreateUser_whenEmailIsInvalid() {
            // arrange
            String invalidEmail = "invalid_email_format";

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UsersModel.of("testUser",
                        Gender.MALE,
                        "1993-04-09",
                        invalidEmail);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void failsToCreateUser_whenBirthDateIsInvalid() {
            // arrange
            String invalidBirthDate = "19931230"; // 잘못된 날짜 형식

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UsersModel.of("testUser",
                        Gender.MALE,
                        invalidBirthDate,
                        "test@gmail.com");
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
