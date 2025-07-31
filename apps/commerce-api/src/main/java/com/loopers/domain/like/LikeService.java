package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeService {
    private final LikeRepository likeRepository;

    @Transactional
    public LikeEntity register(LikeCommand.Product command) {
        var criteria = LikeStatement.builder()
                .userId(command.userId())
                .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, command.targetId())
                .build();
        var existingLikes = likeRepository.findList(criteria);
        if(existingLikes.isEmpty()) {
            LikeSummaryEntity summary = likeRepository.findSummary(command.targetId(), LikeEntity.TargetType.PRODUCT)
                    .orElseGet(() -> LikeSummaryEntity.of(command.targetId(), LikeEntity.TargetType.PRODUCT));
            summary.increaseCount();
            likeRepository.save(summary);
        }

        LikeEntity like = LikeEntity.from(command);
        return likeRepository.save(like);
    }

    @Transactional
    public LikeEntity remove(LikeCommand.Product command) {
        var criteria = LikeStatement.builder()
                .userId(command.userId())
                .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, command.targetId())
                .build();
        var existingLikes = likeRepository.findList(criteria);
        
        // 실제로 좋아요가 존재할 때만 카운트를 감소시킴
        if(!existingLikes.isEmpty()) {
            LikeSummaryEntity summary = likeRepository.findSummary(command.targetId(), LikeEntity.TargetType.PRODUCT)
                    .orElseGet(() -> LikeSummaryEntity.of(command.targetId(), LikeEntity.TargetType.PRODUCT));
            summary.decreaseCount();
            likeRepository.save(summary);
        }

        LikeEntity like = LikeEntity.from(command);
        return likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public List<LikeEntity> find(LikeStatement criteria) {
        return likeRepository.findList(criteria);
    }

    @Transactional(readOnly = true)
    public Optional<LikeSummaryEntity> findSummary(Long targetId, LikeEntity.TargetType targetType) {
        return likeRepository.findSummary(targetId, targetType);
    }

}
