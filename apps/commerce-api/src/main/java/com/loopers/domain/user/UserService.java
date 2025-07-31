package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity create(UserCommand.Create command) {
        var byLoginId = UserStatement.loginId(command.loginId());
        if (userRepository.exists(byLoginId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 로그인 ID입니다.");
        }

        UserEntity userEntity = UserEntity.of(command);
        return userRepository.save(userEntity);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> find(UserStatement userStatement) {
        return userRepository.find(userStatement);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> find(Long id) {
        return userRepository.find(id);
    }
}
