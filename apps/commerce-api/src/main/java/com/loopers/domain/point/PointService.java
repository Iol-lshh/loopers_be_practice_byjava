package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointService {
    private final PointRepository pointRepository;

    @Transactional
    public PointEntity charge(Long userId, Long amount) {
        PointEntity point = pointRepository.findWithLockByUserId(userId)
            .orElse(PointEntity.init(userId));
        point.add(amount);
        return pointRepository.save(point);
    }

    @Transactional(readOnly = true)
    public Optional<PointEntity> findByUserId(Long userId) {
        return pointRepository.findByUserId(userId);
    }

    @Transactional
    public PointEntity init(Long userId) {
        return pointRepository.save(PointEntity.init(userId));
    }

    @Transactional
    public PointEntity pay(Long userId, Long totalPrice) {
        PointEntity point = pointRepository.findWithLockByUserId(userId).orElseThrow(() ->
                new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다."));
        point.subtract(totalPrice);
        return pointRepository.save(point);
    }
}
