package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

    private final PointFacade pointFacade;

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointV1Dto.PointsResponse> charge(
            @RequestBody PointV1Dto.PointsChargeRequest request
    ) {
        var info = pointFacade.charge(request.loginId(), request.point());
        var response = PointV1Dto.PointsResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("")
    @Override
    public ApiResponse<PointV1Dto.PointsResponse> get(
            @RequestHeader("X-USER-ID") String loginId
    ) {
        var info = pointFacade.get(loginId);
        var response = PointV1Dto.PointsResponse.from(info);
        return ApiResponse.success(response);
    }
}
