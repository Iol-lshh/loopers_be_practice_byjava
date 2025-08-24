package com.loopers.domain.payment;

import java.util.Optional;

public interface UserCardRepository {
    UserCardEntity save(UserCardEntity userCardEntity);

    Optional<UserCardEntity> findByUserId(Long aLong);
}
