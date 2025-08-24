package com.loopers.domain.like;

public class LikeEvent {
    public record Increased(
            Long targetId,
            LikeEntity.TargetType targetType
    ) {
        public static Increased from(LikeEntity likeEntity) {
            return new Increased(
                    likeEntity.getTargetId(),
                    likeEntity.getTargetType()
            );
        }
    }

    public record Decreased(
            Long targetId,
            LikeEntity.TargetType targetType
    ) {
        public static Decreased from(LikeEntity likeEntity) {
            return new Decreased(
                    likeEntity.getTargetId(),
                    likeEntity.getTargetType()
            );
        }
    }
}

