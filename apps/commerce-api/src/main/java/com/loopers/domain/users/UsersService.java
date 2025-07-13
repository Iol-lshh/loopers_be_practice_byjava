package com.loopers.domain.users;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.type.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UsersService {

    private final UsersRepository usersRepository;

    @Transactional
    public UsersModel register(String loginId, Gender gender, String birthDate, String email) {
        if (usersRepository.existsByLoginId(loginId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 로그인 ID입니다.");
        }

        UsersModel usersModel = UsersModel.of(loginId, gender, birthDate, email);
        return usersRepository.save(usersModel);
    }

    @Transactional(readOnly = true)
    public UsersModel getMyInfo(String loginId) {
        return usersRepository.getByLoginId(loginId);
    }
}
