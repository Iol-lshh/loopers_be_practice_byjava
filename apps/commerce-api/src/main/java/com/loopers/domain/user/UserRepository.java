package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity model);
    boolean exists(UserCriteria criteria);
    Optional<UserEntity> find(Long id);
    Optional<UserEntity> find(UserCriteria criteria);
}
