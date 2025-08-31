package com.loopers.domain.like;

public class LikeCommand {
    public record Product(
        Long userId,
        Long targetId
    ) {
    }

    public record CreateSummary (
        Long userId,
        Long targetId,
        LikeEntity.TargetType targetType
    ) {
    }
}
