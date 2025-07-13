package com.loopers.infrastructure.users;

import com.loopers.domain.users.UsersModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersJpaRepository extends JpaRepository<UsersModel, Long> {
    boolean existsByLoginId(String loginId);

    Optional<UsersModel> findByLoginId(String loginId);
}
