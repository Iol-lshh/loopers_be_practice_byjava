package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeStatement;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeSummaryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;
    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Override
    public LikeEntity save(LikeEntity like) {
        return likeJpaRepository.save(like);
    }

    public LikeEntity delete(LikeEntity like) {
        likeJpaRepository.delete(like);
        return like;
    }

    @Override
    public List<LikeEntity> findList(LikeStatement criteria) {
        var spec = LikeJpaSpecification.with(criteria);
        return likeJpaRepository.findAll(spec);
    }

    @Override
    public Long count(LikeStatement likeStatement) {
        var spec = LikeJpaSpecification.with(likeStatement);
        return likeJpaRepository.count(spec);
    }

    @Override
    public LikeSummaryEntity save(LikeSummaryEntity summary) {
        return likeSummaryJpaRepository.save(summary);
    }

    @Override
    public Optional<LikeSummaryEntity> findSummary(Long targetId, LikeEntity.TargetType targetType) {
        return likeSummaryJpaRepository.findByTargetIdAndTargetType(targetId, targetType);
    }
}
