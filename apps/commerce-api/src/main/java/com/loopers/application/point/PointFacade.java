package com.loopers.application.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final UserService userService;
    private final PointService pointService;

    @Transactional
    public PointResult get(Long userId) {
        UserEntity targetUser = userService.find(userId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId)
        );
        PointEntity point = pointService.findByUserId(targetUser.getId()).orElse(
                pointService.init(targetUser.getId())
        );
        return PointResult.from(point);
    }

    @Transactional
    public PointResult charge(Long userId, Long amount) {
        UserEntity targetUser = userService.find(userId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId)
        );
        PointEntity point = pointService.charge(targetUser.getId(), amount);
        return PointResult.from(point);
    }
}
