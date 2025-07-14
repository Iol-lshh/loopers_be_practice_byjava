package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.time.DateTimeException;
import java.time.LocalDate;

public class UserValidator {
    public static void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 비어있을 수 없습니다: " + loginId);
        }
        if (loginId.length() > 10) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 10자 이하여야 합니다: " + loginId);
        }
    }

    public static void validateBirthDate(String birthDate) {
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

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 비어있을 수 없습니다: " + email);
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 올바른 형식이어야 합니다: " + email);
        }
    }
}
