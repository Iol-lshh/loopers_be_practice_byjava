package com.loopers.infrastructure.points;

import com.loopers.domain.points.PointsModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsJpaRepository extends JpaRepository<PointsModel, Long> {
}
