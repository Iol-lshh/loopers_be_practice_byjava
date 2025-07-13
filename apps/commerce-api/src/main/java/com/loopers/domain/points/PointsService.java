package com.loopers.domain.points;

import com.loopers.domain.users.UsersModel;
import com.loopers.domain.users.UsersRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointsService {
    private final PointsRepository pointsRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public PointsModel get(String loginId) {
        UsersModel user = usersRepository.getByLoginId(loginId);
        if (user == null) {
            return null;
        }
        PointsModel result = pointsRepository.getByUserId(user.getId());
        if(result == null) {
            result = pointsRepository.save(PointsModel.from(user.getId()));
        }
        return result;
    }

    @Transactional
    public PointsModel charge(String loginId, Long amount) {
        UsersModel user = usersRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        PointsModel model = pointsRepository.findByUserId(user.getId())
                .orElseGet(() -> PointsModel.from(user.getId()));
        model.charge(amount);
        return pointsRepository.save(model);
    }
}
