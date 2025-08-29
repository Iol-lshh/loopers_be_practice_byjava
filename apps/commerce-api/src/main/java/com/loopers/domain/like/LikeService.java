package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeService {
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LikeEntity register(LikeCommand.Product command) {
        var criteria = LikeStatement.builder()
                .userId(command.userId())
                .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, command.targetId())
                .build();
        var existingLikes = likeRepository.findList(criteria);
        LikeEntity like = LikeEntity.from(command);
        if(existingLikes.isEmpty()) {
            LikeEvent.Increased increased = LikeEvent.Increased.from(like);
            eventPublisher.publishEvent(increased);
        }
        return likeRepository.save(like);
    }

    @Transactional
    public LikeSummaryEntity register(LikeCommand.CreateSummary command) {
        LikeSummaryEntity summary = LikeSummaryEntity.of(command.targetId(), command.targetType());
        return likeRepository.save(summary);
    }

    @Transactional
    public void increaseLikeCount(Long targetId, LikeEntity.TargetType targetType) {
        LikeSummaryEntity summary = likeRepository.findSummaryWithLock(targetId, targetType)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요 요약 정보를 찾을 수 없습니다."));
        summary.increaseCount();
        likeRepository.save(summary);
    }

    @Transactional
    public void decreaseLikeCount(Long targetId, LikeEntity.TargetType targetType) {
        LikeSummaryEntity summary = likeRepository.findSummaryWithLock(targetId, targetType)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "좋아요 요약 정보를 찾을 수 없습니다."));
        summary.decreaseCount();
        likeRepository.save(summary);
    }

    @Transactional
    public LikeEntity remove(LikeCommand.Product command) {
        var criteria = LikeStatement.builder()
                .userId(command.userId())
                .likeTypeAndTargetId(LikeEntity.TargetType.PRODUCT, command.targetId())
                .build();
        var existingLikes = likeRepository.findList(criteria);
        LikeEntity like = LikeEntity.from(command);
        if(!existingLikes.isEmpty()) {
            LikeEvent.Decreased decreased = LikeEvent.Decreased.from(like);
            eventPublisher.publishEvent(decreased);
        }
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
