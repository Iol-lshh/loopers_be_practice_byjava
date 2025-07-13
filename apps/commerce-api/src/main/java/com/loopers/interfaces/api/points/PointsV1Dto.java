package com.loopers.interfaces.api.points;

import com.loopers.application.points.PointsInfo;

public class PointsV1Dto {
    public record PointsResponse(
            Long id, Long userId, Long points
    ) {
        public static PointsResponse from(PointsInfo info) {
            return new PointsResponse(
                    info.id(),
                    info.userId(),
                    info.amount()
            );
        }
    }

    public record PointsChargeRequest(
            String loginId, Long points
    ) {

    }
}
