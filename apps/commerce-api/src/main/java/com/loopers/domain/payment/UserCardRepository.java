package com.loopers.domain.payment;

import java.util.Optional;

public interface UserCardRepository {
    Optional<UserCardEntity> find(Long aLong);

    UserCardEntity save(UserCardEntity userCardEntity);
}
