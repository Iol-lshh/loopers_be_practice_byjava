package com.loopers.application.like;

import com.loopers.domain.like.LikeEntity;

public class LikeResult {

    public record Result(
            Long userId,
            String targetType,
            Long targetId,
            Boolean isLike
    ) {

        public static Result of(LikeEntity like, boolean isLike) {
            return new Result(
                    like.getUserId(),
                    like.getTargetType().name(),
                    like.getTargetId(),
                    isLike
            );
        }
    }
}
