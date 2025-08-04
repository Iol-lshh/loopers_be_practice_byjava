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
            @Schema(name = "요청", description = "충전할 사용자 정보와 포인트 수량 입력") PointV1Dto.PointsChargeRequest request
    );

    @Operation(
            summary = "조회",
            description = "포인트 조회"
    )
    ApiResponse<PointV1Dto.PointsResponse> get(
            @Schema(name = "요청", description = "포인트 조회할 사용자 정보 입력") Long userId
    );
}
