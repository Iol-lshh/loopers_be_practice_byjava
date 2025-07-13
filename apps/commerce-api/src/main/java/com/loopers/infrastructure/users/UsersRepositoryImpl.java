package com.loopers.infrastructure.users;

import com.loopers.domain.users.UsersModel;
import com.loopers.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UsersRepositoryImpl implements UsersRepository {
    private final UsersJpaRepository jpaRepository;

    @Override
    public UsersModel save(UsersModel model) {
        return jpaRepository.save(model);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return jpaRepository.existsByLoginId(loginId);
    }

    @Override
    public Optional<UsersModel> findByLoginId(String loginId) {
        return jpaRepository.findByLoginId(loginId);
    }
}
