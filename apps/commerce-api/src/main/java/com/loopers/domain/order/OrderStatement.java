package com.loopers.domain.order;

import java.util.List;

public record OrderStatement(
        List<Criterion> criteria
) {
    public interface Criterion{}

    public record UserId(Long userId) implements Criterion {}

    public static OrderStatement userId(Long userId) {
        return new OrderStatement(List.of(new UserId(userId)));
    }
}
