package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserCriteria;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository jpaRepository;

    @Override
    public UserEntity save(UserEntity model) {
        return jpaRepository.save(model);
    }

    @Override
    public boolean exists(UserCriteria criteria) {
        var spec = UserJpaSpecification.with(criteria);
        return jpaRepository.exists(spec);
    }

    @Override
    public Optional<UserEntity> find(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> find(UserCriteria criteria) {
        var spec = UserJpaSpecification.with(criteria);
        var list = jpaRepository.findAll(spec);
        if (list.size() > 1) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "More than one user found");
        }
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }
}
