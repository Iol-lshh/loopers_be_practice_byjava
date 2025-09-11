package com.loopers.domain.like;

public interface LikeGlobalEventPublisher {
    void publish(LikeEvent.Updated event);
}
