package com.loopers.interfaces.api.points;

import com.loopers.application.points.PointsFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointsV1Controller {

    private final PointsFacade pointsFacade;

    @PostMapping("/charge")
    public ApiResponse<PointsV1Dto.PointsResponse> charge(
            @RequestBody PointsV1Dto.PointsChargeRequest request
    ) {
        var info = pointsFacade.charge(request.loginId(), request.points());
        var response = PointsV1Dto.PointsResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("")
    public ApiResponse<PointsV1Dto.PointsResponse> get(
            @RequestHeader("X-USER-ID") String loginId
    ) {
        var info = pointsFacade.get(loginId);
        var response = PointsV1Dto.PointsResponse.from(info);
        return ApiResponse.success(response);
    }
}
