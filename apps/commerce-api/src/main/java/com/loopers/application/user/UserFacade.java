package com.loopers.application.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    @Transactional
    public UserResult signUp(UserCommand.Create command) {
        UserEntity model = userService.create(command);
        return UserResult.from(model);
    }

    @Transactional(readOnly = true)
    public UserResult get(Long userId) {
        UserEntity user = userService.find(userId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));
        return UserResult.from(user);
    }
}
