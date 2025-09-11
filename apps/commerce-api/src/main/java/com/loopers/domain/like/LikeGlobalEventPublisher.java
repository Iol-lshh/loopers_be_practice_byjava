package com.loopers.domain.like;

import com.loopers.events.commerce.LikeV1Event;

public interface LikeGlobalEventPublisher {
    void publish(LikeV1Event.Updated event);
}
