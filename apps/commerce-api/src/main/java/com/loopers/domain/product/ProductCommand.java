package com.loopers.domain.product;

public class ProductCommand {
    public record Register(
            String name,
            Long brandId,
            Long price,
            Long stock
    ) {
    }
}
