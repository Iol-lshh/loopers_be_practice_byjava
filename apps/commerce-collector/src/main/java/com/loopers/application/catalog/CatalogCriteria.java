package com.loopers.application.catalog;

public class CatalogCriteria {
    public record OutOfStock(
            String eventId,
            Long productId,
            Object messages
    ){

    }
}
