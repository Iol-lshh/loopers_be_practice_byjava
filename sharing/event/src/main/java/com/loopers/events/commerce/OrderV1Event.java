package com.loopers.events.commerce;

import java.util.List;
import java.util.Map;

public class OrderV1Event {
    public static final class TOPIC{
        public static final String REGISTERED = "internal.order-registered.v1";
        public static final String COMPLETED = "internal.order-completed.v1";
        public static final String CANCELED = "internal.order-canceled.v1";
    }

    public record Registered(
            Long userId,
            Long orderId,
            Long totalPrice,
            String paymentType
    ) {
    }

    public record Completed(
            Long orderId,
            Long userId,
            Long totalPrice,
            String paymentType,
            List<Long> couponIds,
            Map<Long, Long> itemQuantityMap
    ) {
    }

    public record Canceled(
            Long orderId,
            Long userId,
            Long totalPrice,
            String paymentType
    ){
    }
}
