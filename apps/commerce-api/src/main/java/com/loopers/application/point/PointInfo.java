package com.loopers.application.point;

import com.loopers.domain.user.UserEntity;

public record PointInfo(
        Long userId,
        Long point
) {

    public static PointInfo from(UserEntity user) {
        return new PointInfo(
                user.getId(),
                user.getPoint().getAmount()
        );
    }
}
