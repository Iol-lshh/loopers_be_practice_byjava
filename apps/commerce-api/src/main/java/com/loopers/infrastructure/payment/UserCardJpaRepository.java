package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.UserCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCardJpaRepository extends JpaRepository<UserCardEntity, Long> {
}
