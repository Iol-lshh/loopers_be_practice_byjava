package com.loopers.domain.like;

public class LikeCommand {
    public record Product(
        Long userId,
        Long targetId
    ) {
    }
}
