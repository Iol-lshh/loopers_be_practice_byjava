package com.loopers.domain.like;

import java.util.List;

public record LikeStatement(
    List<Criterion> criteria
) {
    public interface Criterion {}
    public record UserId(Long userId) implements Criterion {}
    public record LikeTypeAndTargetId(LikeEntity.TargetType targetType, Long targetId) implements Criterion {}
    public record LikeTypeAndTargetIds(LikeEntity.TargetType targetType, List<Long> targetIds) implements Criterion {}

    public static Builder builder() {return new Builder();}

    public static class Builder {
        private final List<Criterion> criteria;

        public Builder() {
            this.criteria = new java.util.ArrayList<>();
        }

        public Builder userId(Long userId) {
            this.criteria.add(new UserId(userId));
            return this;
        }

        public Builder likeTypeAndTargetId(LikeEntity.TargetType targetType, Long targetId) {
            this.criteria.add(new LikeTypeAndTargetId(targetType, targetId));
            return this;
        }

        public Builder likeTypeAndTargetIds(LikeEntity.TargetType targetType, List<Long> targetIds) {
            this.criteria.add(new LikeTypeAndTargetIds(targetType, targetIds));
            return this;
        }

        public LikeStatement build() {
            return new LikeStatement(List.copyOf(criteria));
        }
    }
}
