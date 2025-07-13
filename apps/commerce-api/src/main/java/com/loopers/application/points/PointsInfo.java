package com.loopers.application.points;

import com.loopers.domain.points.PointsModel;

public record PointsInfo(
        Long id,
        Long userId,
        Long amount
) {

    public static PointsInfo from(PointsModel model) {
        return new PointsInfo(
                model.getId(),
                model.getUserId(),
                model.getAmount()
        );
    }
}
