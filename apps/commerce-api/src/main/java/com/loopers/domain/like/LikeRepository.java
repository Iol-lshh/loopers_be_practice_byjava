package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    LikeEntity save(LikeEntity like);

    LikeEntity delete(LikeEntity like);

    List<LikeEntity> findList(LikeStatement criteria);

    LikeSummaryEntity save(LikeSummaryEntity summary);

    Optional<LikeSummaryEntity> findSummary(Long targetId, LikeEntity.TargetType targetType);

    Optional<LikeSummaryEntity> findSummaryWithLock(Long targetId, LikeEntity.TargetType targetType);
}
