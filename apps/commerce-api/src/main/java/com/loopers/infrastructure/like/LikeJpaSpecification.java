package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeStatement;
import com.loopers.domain.like.LikeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class LikeJpaSpecification {
    public static Specification<LikeEntity> with(LikeStatement criteria) {
        return (root, query, criteriaBuilder) -> {

            Predicate predicate = criteriaBuilder.conjunction();
            for(var criterion: criteria.criteria()) {

                if (criterion instanceof LikeStatement.UserId(Long userId)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("userId"), userId));

                }

                if (criterion instanceof
                        LikeStatement.LikeTypeAndTargetIds(LikeEntity.TargetType targetType, List<Long> targetIds)
                ) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("targetType"), targetType),
                                    criteriaBuilder.in(root.get("targetId")).value(targetIds)
                            ));
                }
            }

            return predicate;
        };
    }
}
