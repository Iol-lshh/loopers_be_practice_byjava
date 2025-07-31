package com.loopers.domain.user;

import java.util.List;

public record UserStatement(
        List<Criterion> criteria
) {
    public interface Criterion {}

    public record LoginId(
            String loginId
    ) implements Criterion {}

    public static UserStatement loginId(String loginId) {

        return new UserStatement(List.of(new LoginId(loginId)));
    }
}
