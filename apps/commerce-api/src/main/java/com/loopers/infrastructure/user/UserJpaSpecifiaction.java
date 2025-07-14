package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserCriteria;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserJpaSpecifiaction {
    public static Specification<UserEntity> with(UserCriteria criteria) {
        return (root, query, criteriaBuilder) -> {

            Predicate predicate = criteriaBuilder.conjunction();

            if (criteria.getLoginId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("loginId"), criteria.getLoginId()));
            }
            return predicate;
        };
    }
}
