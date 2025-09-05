package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeGlobalEventPublisher;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.LikeV1Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeGlobalEventPublisherImpl implements LikeGlobalEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Override
    public void publish(LikeEvent.Updated event) {
        var message = GlobalEvent.of(LikeV1Event.TOPIC.UPDATED, event);
        kafkaTemplate.send(
                LikeV1Event.TOPIC.UPDATED,
                event.targetType() + "-" + event.targetId(),
                message
        );
    }
}
