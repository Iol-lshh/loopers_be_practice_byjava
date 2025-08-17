package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeSummaryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LikeSummaryJpaRepository extends CrudRepository<LikeSummaryEntity, LikeSummaryEntity.LikeSummaryId> {
    Optional<LikeSummaryEntity> findByTargetIdAndTargetType(Long targetId, LikeEntity.TargetType targetType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ls FROM LikeSummaryEntity ls WHERE ls.targetId = :targetId AND ls.targetType = :targetType")
    Optional<LikeSummaryEntity> findWithLock(Long targetId, LikeEntity.TargetType targetType);
}
