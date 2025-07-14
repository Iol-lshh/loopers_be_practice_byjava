package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {
    @Nested
    class Create {
        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "invalid_login_id"
        })
        void failsToCreateUser_whenLoginIdIsInvalid(String invalidLoginId) {
            // arrange
            UserCommand.Create command = new UserCommand.Create(
                    invalidLoginId,
                    Gender.MALE,
                    "1993-04-09",
                    "test@gmail.com"
            );

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UserEntity.of(command);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "invalid_email_format"
        })
        void failsToCreateUser_whenEmailIsInvalid(String invalidEmail) {
            // arrange
            UserCommand.Create command = new UserCommand.Create(
                    "testId",
                    Gender.MALE,
                    "1993-04-09",
                    invalidEmail
            );

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UserEntity.of(command);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "19931230"
        })
        void failsToCreateUser_whenBirthDateIsInvalid(String invalidBirthDate) {
            // arrange
            UserCommand.Create command = new UserCommand.Create(
                    "testId",
                    Gender.MALE,
                    invalidBirthDate,
                    "test@gmail.com"
            );

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UserEntity.of(command);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
