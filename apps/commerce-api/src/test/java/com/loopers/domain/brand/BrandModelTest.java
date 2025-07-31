package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
class BrandModelTest {
    @DisplayName("생성")
    @Nested
    class Create {
        @DisplayName("브랜드 이름이 비어있으면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        void failsToCreateBrand_whenNameIsEmpty(String invalidBrandName) {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> BrandEntity.of(invalidBrandName));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("브랜드 이름이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateBrand_whenNameIsNull() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () ->
                    BrandEntity.of(null));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

    }

}
