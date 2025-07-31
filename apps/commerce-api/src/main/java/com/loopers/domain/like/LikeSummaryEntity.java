package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "like_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeSummaryEntity extends BaseEntity {
    private Long likeCount;
    private Long targetId;
    private LikeEntity.TargetType targetType;

    public LikeSummaryEntity(Long targetId, LikeEntity.TargetType targetType, long likeCount) {
        super();
        if (targetId == null) {
            throw new IllegalArgumentException("대상 ID는 null일 수 없습니다.");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("대상 타입은 null일 수 없습니다.");
        }
        if (likeCount < 0) {
            throw new IllegalArgumentException("좋아요 수는 null이거나 음수일 수 없습니다.");
        }
        this.targetId = targetId;
        this.targetType = targetType;
        this.likeCount = likeCount;
    }

    public static LikeSummaryEntity of(Long targetId, LikeEntity.TargetType targetType) {
        return new LikeSummaryEntity(
                targetId,
                targetType,
                0L
        );
    }

    public void increaseCount() {
        this.likeCount++;
    }

    public void decreaseCount() {
        if (this.likeCount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "좋아요 수가 이미 0입니다.");
        }
        this.likeCount--;
    }
}
