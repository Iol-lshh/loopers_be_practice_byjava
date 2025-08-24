package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.UserCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCardJpaRepository extends JpaRepository<UserCardEntity, Long> {
    Optional<UserCardEntity> findByUserId(Long userId);
}
