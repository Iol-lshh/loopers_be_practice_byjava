package com.loopers.application.point;

import com.loopers.domain.user.UserCriteria;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final UserService userService;

    @Transactional
    public PointInfo get(String loginId) {
        UserCriteria criteria = UserCriteria.byLoginId(loginId);
        var targetUser = userService.find(criteria);
        if(targetUser.isEmpty()) {
            return null;
        }
        return PointInfo.from(targetUser.get());
    }

    @Transactional
    public PointInfo charge(String loginId, Long amount) {
        UserCriteria criteria = UserCriteria.byLoginId(loginId);
        var targetUser = userService.find(criteria);
        if(targetUser.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다: " + loginId);
        }
        targetUser.get().charge(amount);
        return PointInfo.from(targetUser.get());
    }
}
