package com.loopers.application.users;

import com.loopers.domain.users.UsersModel;

public record UsersInfo (
        Long id,
        String loginId,
        String gender,
        String birthDate,
        String email
) {
    public static UsersInfo from(UsersModel model) {
        return new UsersInfo(
                model.getId(),
                model.getLoginId(),
                model.getGender().getValue(),
                model.getBirthDate(),
                model.getEmail()
        );
    }
}
