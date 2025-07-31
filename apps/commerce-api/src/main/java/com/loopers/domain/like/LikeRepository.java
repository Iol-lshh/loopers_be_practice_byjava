package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    LikeEntity save(LikeEntity like);

    LikeEntity delete(LikeEntity like);

    List<LikeEntity> findList(LikeStatement criteria);

    Long count(LikeStatement likeStatement);

    LikeSummaryEntity save(LikeSummaryEntity summary);

    Optional<LikeSummaryEntity> findSummary(Long targetId, LikeEntity.TargetType targetType);
}
