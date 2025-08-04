package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeSummaryEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LikeSummaryJpaRepository extends CrudRepository<LikeSummaryEntity, Long> {
    Optional<LikeSummaryEntity> findByTargetIdAndTargetType(Long targetId, LikeEntity.TargetType targetType);
}
