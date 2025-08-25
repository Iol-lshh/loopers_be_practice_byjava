package com.loopers.domain.product;

public class ProductEvent {
    public record Registered(Long productId) {

        public static Registered from(ProductEntity product) {
            return new Registered(product.getId());
        }
    }
}
