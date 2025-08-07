package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<PointEntity> findByUserId(Long id);

    PointEntity save(PointEntity point);

    Optional<PointEntity> findWithLockByUserId(Long userId);
}
