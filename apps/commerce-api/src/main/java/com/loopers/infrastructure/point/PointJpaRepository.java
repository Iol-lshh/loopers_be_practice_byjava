package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {
}
