package com.loopers.application.point;

import com.loopers.domain.point.PointEntity;

public record PointResult(
        Long userId,
        Long pointId,
        Long amount
) {

    public static PointResult from(PointEntity point) {
        return new PointResult(
                point.getUserId(),
                point.getId(),
                point.getAmount()

        );
    }
}
