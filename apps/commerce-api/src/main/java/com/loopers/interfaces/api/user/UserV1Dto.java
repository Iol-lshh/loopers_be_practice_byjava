package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.UserCommand;

public class UserV1Dto {
    public record UsersResponse(
            Long id, String loginId, String gender, String birthDate, String email
    ) {
        public static UsersResponse from(UserInfo info) {
            return new UsersResponse(
                info.id(),
                info.loginId(),
                info.gender(),
                info.birthDate(),
                info.email()
            );
        }
    }


    public record UsersSignUpRequest(
            String loginId, String gender, String birthDate, String email
    ) {
        public UserCommand.Create toCommand() {
            return UserCommand.Create.of(loginId, gender, birthDate, email);
        }
    }
}
