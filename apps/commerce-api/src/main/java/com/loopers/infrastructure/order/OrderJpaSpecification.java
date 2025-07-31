package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderStatement;
import com.loopers.domain.order.OrderEntity;
import org.springframework.data.jpa.domain.Specification;

public class OrderJpaSpecification {
    public static Specification<OrderEntity> with(OrderStatement criteria) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            for (var criterion : criteria.criteria()) {
                if (criterion instanceof OrderStatement.UserId(Long userId)) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(root.get("userId"), userId));

                }
            }

            return predicate;
        };
    }
}
