package com.loopers.domain.user;

import java.util.List;

public record UserCriteria(
        List<Criterion> criteria
) {
    public interface Criterion {}

    public record ByLoginId(
            String loginId
    ) implements Criterion {}

    public static UserCriteria byLoginId(String loginId) {

        return new UserCriteria(List.of(new ByLoginId(loginId)));
    }
}
