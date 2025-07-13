package com.loopers.infrastructure.points;

import com.loopers.domain.points.PointsModel;
import com.loopers.domain.points.PointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointsRepositoryImpl implements PointsRepository {

    private final PointsJpaRepository jpaRepository;

    @Override
    public Optional<PointsModel> findByUserId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public PointsModel save(PointsModel model) {
        return jpaRepository.save(model);
    }
}
