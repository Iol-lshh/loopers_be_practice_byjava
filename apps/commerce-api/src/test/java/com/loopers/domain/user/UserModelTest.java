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
                "",
                "abcdefghijk", "12345678901",
                " test", "test ", "te st",
                "테스트", "テスト", "测试",
                "abc_def", "abc.def", "abc-def",
        })
        void failsToCreateUser_whenLoginIdIsInvalid(String invalidLoginId) {
            // arrange
            UserCommand.Create command = UserCommand.Create.of(
                    invalidLoginId,
                    "남",
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
                "",
                "invalid_email_format", "invalid@domain",
                "invalid@domain.", "invalid@.com",
                "invalid@domain..com", "@domain.com",
                "@.com", "invalid@domain.c@om",
                "@.", "a@b.c",
        })
        void failsToCreateUser_whenEmailIsInvalid(String invalidEmail) {
            // arrange
            UserCommand.Create command = UserCommand.Create.of(
                    "testId",
                    "남",
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
                "", "-", "--", "---", " - - ", "  -  -  ",
                "19931230", "199312-30", "1993-1230", "1993-12-30-", "1993-12-30-1", 
                "1993-1-30", "1993-12-3", "1993-12-300", "1993-12-300",
                "3000-01-01",
        })
        void failsToCreateUser_whenBirthDateIsInvalid(String invalidBirthDate) {
            // arrange
            UserCommand.Create command = UserCommand.Create.of(
                    "testId",
                    "남",
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

        @DisplayName("성별이 정의된 값이 아니면, UserCommand.Create 생성에 실패한다.")
        @ParameterizedTest()
        @ValueSource(strings = {
                "",
                "남성", "여성", "기타",
                "남성 ", " 여 성", "기타 ",
                "남성입니다", "여성입니다", "기타입니다", "남성_여성", "남성.여성", "남성-여성",
                "@", "_", "123", "남성123", "여성123", "기타123",
                "tester", "1993-12-30", "test@gmail.com"
        })
        void failsToCreateUser_whenGenderIsInvalid(String invalidGender) {

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                UserCommand.Create.of(
                        "testId",
                        invalidGender,
                        "1993-04-09",
                        "test@gmail.com"
                );
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
