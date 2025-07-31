package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;

public record ProductStatement(
        List<Criterion> criteria
) {
    public interface Criterion {}
    public interface OrderBy extends Criterion {}
    public interface Where extends Criterion {}

    public record CreatedAt(boolean ascending) implements OrderBy {}
    public record ReleasedAt(boolean ascending) implements OrderBy {}
    public record Price(boolean ascending) implements OrderBy {}
    public record LikeCount() implements OrderBy {}

    public record BrandID(Long brandId) implements Where {}
    public record State(ProductEntity.State.StateType state) implements Where {}
    public record UserId(Long userId) implements Where {}

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private final List<Criterion> criteria;

        public Builder() {
            this.criteria = new java.util.ArrayList<>();
        }

        public Builder orderBy(OrderBy orderBy){
            if (this.criteria.stream().anyMatch(c -> c instanceof OrderBy)) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "이미 정렬 기준이 설정되어 있습니다.");
            }
            this.criteria.add(orderBy);
            return this;
        }

        public Builder brandID(Long brandId) {
            this.criteria.add(new BrandID(brandId));
            return this;
        }

        public Builder userId(Long userId) {
            this.criteria.add(new UserId(userId));
            return this;
        }

        public ProductStatement build() {
            if (this.criteria.stream().noneMatch(c -> c instanceof OrderBy)) {
                this.criteria.add(new ReleasedAt(false));
            }
            if (this.criteria.stream().noneMatch(c -> c instanceof State)) {
                this.criteria.add(new State(ProductEntity.State.StateType.OPEN));
            }

            return new ProductStatement(List.copyOf(criteria));
        }
    }
}
