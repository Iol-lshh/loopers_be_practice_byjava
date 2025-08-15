package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Points V1 API", description = "포인트 충전, 조회")
public interface PointV1ApiSpec {

    @Operation(
            summary = "충전",
            description = "포인트 충전"
    )
    ApiResponse<PointV1Dto.PointsResponse> charge(
            PointV1Dto.PointsChargeRequest request
    );

    @Operation(
            summary = "조회",
            description = "포인트 조회"
    )
    ApiResponse<PointV1Dto.PointsResponse> get(
            Long userId
    );
}
