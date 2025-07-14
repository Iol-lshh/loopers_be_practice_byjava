package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;

public class PointV1Dto {
    public record PointsResponse(
            Long userId, Long point
    ) {
        public static PointsResponse from(PointInfo info) {
            return new PointsResponse(
                    info.userId(),
                    info.point()
            );
        }
    }

    public record PointsChargeRequest(
            String loginId, Long point
    ) {

    }
}
