package com.loopers.domain.user;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserCriteria {
    private final List<Criterion> criteria;

    public UserCriteria(){
        this.criteria = new ArrayList<>();
    }

    public interface Criterion {}

    public record ByLoginId(
            String loginId
    ) implements Criterion {}

    public static UserCriteria byLoginId(String loginId) {
        UserCriteria specification = new UserCriteria();
        specification.criteria.add(new ByLoginId(loginId));
        return specification;
    }
}
