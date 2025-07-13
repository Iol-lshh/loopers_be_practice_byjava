package com.loopers.domain.users;

import java.util.Optional;

public interface UsersRepository {
    UsersModel save(UsersModel model);
    boolean existsByLoginId(String loginId);


    default UsersModel getByLoginId(String loginId){
        return findByLoginId(loginId)
                .orElse(null);
    }

    Optional<UsersModel> findByLoginId(String loginId);
}
