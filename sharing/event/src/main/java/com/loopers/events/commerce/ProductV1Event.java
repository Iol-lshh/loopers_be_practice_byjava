package com.loopers.events.commerce;

public class ProductV1Event {

    public static final class TOPIC {
        public static final String OUT_OF_STOCK = "internal.product-out-of-stock.v1";
    }

    public record OutOfStock(
            Long productId,
            String eventId
    ) {
    }
}
