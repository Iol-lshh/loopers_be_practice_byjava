package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LikeModelTest {

    @DisplayName("좋아요 생성")
    @Nested
    class Create {
        @DisplayName("좋아요 생성 시 유효하지 않은 사용자 ID가 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateLike_whenUserIdIsNull() {
            // given
            Long invalidUserId = null;
            Long targetId = 1L; // 예시 타겟 ID
            LikeCommand.Product command = new LikeCommand.Product(invalidUserId, targetId);

            // when
            CoreException exception = assertThrows(CoreException.class, () -> LikeEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("좋아요 생성 시 타겟 ID가 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateLike_whenTargetIdIsNull() {
            // given
            Long userId = 1L; // 예시 사용자 ID
            Long invalidTargetId = null;
            LikeCommand.Product command = new LikeCommand.Product(userId, invalidTargetId);

            // when
            CoreException exception = assertThrows(CoreException.class, () -> LikeEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("좋아요 생성 시 좋아요 타입이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateLike_whenLikeTypeIsNull() {
            // given
            Long userId = 1L; // 예시 사용자 ID
            Long targetId = 1L; // 예시 타겟 ID

            // when
            CoreException exception = assertThrows(CoreException.class, () ->
                    new LikeEntity(userId, targetId, null));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }

}
