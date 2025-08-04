package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record Item(
        Long productId,
        Long price,
        Long quantity
    ) {
    }

    public record Order(
        Long userId,
        List<Item> orderItems
    ) {
    }
}
