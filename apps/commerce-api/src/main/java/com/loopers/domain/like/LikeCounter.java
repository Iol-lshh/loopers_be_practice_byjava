package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeCounter {
    private final LikeRepository likeRepository;

    public void increaseLikeCount(Long targetId, LikeEntity.TargetType targetType) {
        LikeSummaryEntity summary = likeRepository.findSummaryWithLock(targetId, targetType)
                .orElseGet(() -> LikeSummaryEntity.of(targetId, targetType));
        summary.increaseCount();
        likeRepository.save(summary);
    }

    public void decreaseLikeCount(Long targetId, LikeEntity.TargetType targetType) {
        LikeSummaryEntity summary = likeRepository.findSummaryWithLock(targetId, targetType)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 요약 정보를 찾을 수 없습니다."));
        summary.decreaseCount();
        likeRepository.save(summary);
    }
} 
