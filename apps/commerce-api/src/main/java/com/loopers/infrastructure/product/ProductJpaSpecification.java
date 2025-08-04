package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductStatement;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductWithSignalEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductJpaSpecification {

    public static Specification<ProductEntity> from(ProductStatement criteria) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            
            for(var criterion: criteria.criteria()) {
                if (criterion instanceof ProductStatement.ReleasedAt(boolean ascending)) {
                    if (ascending) {
                        query.orderBy(criteriaBuilder.asc(root.get("releasedAt")));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get("releasedAt")));
                    }
                }
                if (criterion instanceof ProductStatement.CreatedAt(boolean ascending)) {
                    if (ascending){
                        query.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                    }
                }
                if (criterion instanceof ProductStatement.Price(boolean ascending)) {
                    if (ascending) {
                        query.orderBy(criteriaBuilder.asc(root.get("price")));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get("price")));
                    }
                }
                if (criterion instanceof ProductStatement.LikeCount()) {
                    query.orderBy(criteriaBuilder.desc(root.get("likeCount")));
                }
                if (criterion instanceof ProductStatement.BrandID(Long brandId)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("brandId"), brandId));
                }
                if (criterion instanceof ProductStatement.State(ProductEntity.State.StateType state)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("state").get("state"), state));
                }
                if (criterion instanceof ProductStatement.UserId(Long userId)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("userId"), userId));
                }
            }
            
            return predicate;
        };
    }

    public static Specification<ProductWithSignalEntity> withSignalFrom(ProductStatement criteria) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            for(var criterion: criteria.criteria()) {
                if (criterion instanceof ProductStatement.ReleasedAt(boolean ascending)) {
                    if (ascending) {
                        query.orderBy(criteriaBuilder.asc(root.get("state").get("releasedAt")));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get("state").get("releasedAt")));
                    }
                } else if (criterion instanceof ProductStatement.CreatedAt(boolean ascending)) {
                    if (ascending){
                        query.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                    }
                } else if (criterion instanceof ProductStatement.Price(boolean ascending)) {
                    if (ascending) {
                        query.orderBy(criteriaBuilder.asc(root.get("price")));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get("price")));
                    }
                } else if (criterion instanceof ProductStatement.LikeCount()) {
                    // 직접 likeCount 필드에 접근
                    query.orderBy(criteriaBuilder.desc(root.get("likeCount")));
                }
                if (criterion instanceof ProductStatement.BrandID(Long brandId)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("brandId"), brandId));
                }
            }

            return predicate;
        };
    }
}
