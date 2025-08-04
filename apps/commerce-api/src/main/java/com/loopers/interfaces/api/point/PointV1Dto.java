package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointResult;

public class PointV1Dto {
    public record PointsResponse(
            Long userId, Long amount
    ) {
        public static PointsResponse from(PointResult info) {
            return new PointsResponse(
                    info.userId(),
                    info.amount()
            );
        }
    }

    public record PointsChargeRequest(
            Long userId, Long amount
    ) {

    }
}
