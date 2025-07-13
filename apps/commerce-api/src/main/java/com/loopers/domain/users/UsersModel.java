package com.loopers.domain.users;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.type.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.DateTimeException;
import java.time.LocalDate;

@Getter
@Slf4j
@Entity
@Table(name = "users")
public class UsersModel extends BaseEntity {

    private String loginId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    private String birthDate;

    private String email;

    protected UsersModel() {}

    public UsersModel(String loginId, Gender gender, String birthDate, String email) {
        super();
        validateLoginId(loginId);
        validateBirthDate(birthDate);
        validateEmail(email);
        this.loginId = loginId;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
    }

    public static UsersModel of(String loginId, Gender gender, String birthDate, String email) {
        return new UsersModel(loginId, gender, birthDate, email);
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 비어있을 수 없습니다: " + loginId);
        }
        if (loginId.length() > 10) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 10자 이하여야 합니다: " + loginId);
        }
    }

    private void validateBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 비어있을 수 없습니다: " + birthDate);
        }
        if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 yyyy-MM-dd 형식이어야 합니다: " + birthDate);
        }
        try {
            LocalDate.parse(birthDate);
        } catch (DateTimeException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 yyyy-MM-dd 형식이어야 합니다: " + birthDate);
        }
        if (LocalDate.now().isBefore(LocalDate.parse(birthDate))) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 오늘 이전이어야 합니다: " + birthDate);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 비어있을 수 없습니다: " + email);
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 올바른 형식이어야 합니다: " + email);
        }
    }
}
