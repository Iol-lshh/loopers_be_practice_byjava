package com.loopers.application.points;

import com.loopers.domain.points.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointsFacade {
    private final PointsService pointsService;

    public PointsInfo get(String loginId) {
        var model = pointsService.get(loginId);
        return PointsInfo.from(model);
    }

    public PointsInfo charge(String loginId, Long amount) {
        var model = pointsService.charge(loginId, amount);
        return PointsInfo.from(model);
    }
}
