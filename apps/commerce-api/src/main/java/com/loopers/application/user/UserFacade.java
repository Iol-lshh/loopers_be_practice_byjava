package com.loopers.application.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserCriteria;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo signUp(UserCommand.Create command) {
        UserEntity model = userService.create(command);
        return UserInfo.from(model);
    }

    public UserInfo get(String loginId) {
        var byLoginId = UserCriteria.byLoginId(loginId);
        var optional = userService.find(byLoginId);
        if (optional.isEmpty()) {
            return null;
        }
        return UserInfo.from(optional.get());
    }
}
