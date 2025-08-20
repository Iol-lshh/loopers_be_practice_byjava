package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.UserCardEntity;
import com.loopers.domain.payment.UserCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCardRepositoryImpl implements UserCardRepository {
    private final UserCardJpaRepository jpaRepository;

    @Override
    public Optional<UserCardEntity> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public UserCardEntity save(UserCardEntity userCardEntity) {
        return jpaRepository.save(userCardEntity);
    }
}
