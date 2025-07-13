package com.loopers.application.users;

import com.loopers.domain.users.UsersModel;
import com.loopers.domain.users.UsersService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.type.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UsersFacade {
    private final UsersService usersService;

    public UsersInfo register(String loginId, Gender gender, String birthDate, String email) {
        UsersModel model = usersService.register(loginId, gender, birthDate, email);
        return UsersInfo.from(model);
    }

    public UsersInfo getMyInfo(String loginId) {
        UsersModel model = usersService.getMyInfo(loginId);
        if (model == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다: " + loginId);
        }
        return UsersInfo.from(model);
    }
}
