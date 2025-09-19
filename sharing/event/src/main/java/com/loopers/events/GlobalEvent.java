package com.loopers.events;

import com.loopers.support.uuid.UuidV7Generator;

import java.time.LocalDateTime;

public record GlobalEvent<T>(
        String eventId,
        String topic,
        T payload,
        LocalDateTime invokedAt
) {

    public static <T> GlobalEvent<T> of(String eventId, String topic, T payload) {
        LocalDateTime invokedAt = LocalDateTime.now();
        return new GlobalEvent<>(eventId, topic, payload, invokedAt);
    }

    public static <T> GlobalEvent<T> of(String topic, T payload) {
        String eventId = UuidV7Generator.generateUuidV7().toString();
        return of(eventId, topic, payload);
    }
}
