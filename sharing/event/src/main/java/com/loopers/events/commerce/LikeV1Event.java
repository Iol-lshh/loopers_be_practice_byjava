package com.loopers.events.commerce;

public class LikeV1Event {
    public static final class TOPIC {
        public static final String UPDATED = "internal.like-updated.v1";
    }

    public record Updated(
            Long productId,
            Long count
    ) {
    }
}
