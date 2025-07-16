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
        var byLoginId = UserCriteria.byLoginId(command.loginId());
        if (userRepository.exists(byLoginId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 로그인 ID입니다.");
        }

        UserEntity userEntity = UserEntity.of(command);
        return userRepository.save(userEntity);
    }

    @Transactional
    public Optional<UserEntity> find(UserCriteria userCriteria) {
        return userRepository.find(userCriteria);
    }
}
