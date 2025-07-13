package com.loopers.interfaces.api.users;

import com.loopers.application.users.UsersInfo;

public class UsersV1Dto {
    public record UsersResponse(
            Long id, String loginId, String gender, String birthDate, String email
    ) {
        public static UsersResponse from(UsersInfo info) {
            return new UsersResponse(
                info.id(),
                info.loginId(),
                info.gender(),
                info.birthDate(),
                info.email()
            );
        }
    }


    public record UsersRegisterRequest(
            String loginId, String gender, String birthDate, String email
    ) {
    }
}
