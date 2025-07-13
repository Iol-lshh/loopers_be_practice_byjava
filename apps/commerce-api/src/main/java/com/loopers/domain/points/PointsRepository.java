package com.loopers.domain.points;

import java.util.Optional;

public interface PointsRepository {

    default PointsModel getByUserId(Long userId){
        return findByUserId(userId)
                .orElse(null);
    }

    Optional<PointsModel> findByUserId(Long id);

    PointsModel save(PointsModel model);
}
