package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserStatement;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserJpaSpecification {
    public static Specification<UserEntity> with(UserStatement criteria) {
        return (root, query, criteriaBuilder) -> {

            Predicate predicate = criteriaBuilder.conjunction();
            for(var criterion: criteria.criteria()) {
                if (criterion instanceof UserStatement.LoginId(String loginId)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("loginId"), loginId));
                    break;
                }
            }
            // Add more criteria handling as needed

            return predicate;
        };
    }
}
