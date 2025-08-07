package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class CouponModelTest {

    @DisplayName("쿠폰 생성")
    @Nested
    class Create {
        @DisplayName("쿠폰 타입이 null 이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateCoupon_whenTypeIsNull() {
            // given
            String type = null;
            Long value = 1000L;

            // when
            CoreException exception = assertThrows(CoreException.class, () ->
                new CouponCommand.Admin.Create(type, value).toEntity()
            );

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("쿠폰 값이 null 이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateCoupon_whenValueIsNull() {
            // given
            String type = "FIXED";
            Long value = null;

            // when
            CoreException exception = assertThrows(CoreException.class, () ->
                new CouponCommand.Admin.Create(type, value).toEntity()
            );

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("쿠폰 값이 잘못된 값이면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {-1, Long.MIN_VALUE})
        void failsToCreateCoupon_whenTypeIsInvalid(Long invalidValue) {
            // given
            String type = "FIXED";

            // when
            CoreException exception = assertThrows(CoreException.class, () ->
                new CouponCommand.Admin.Create(type, invalidValue).toEntity()
            );

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("정액 - 정상적인 값들이 제공될 때, 쿠폰이 생성된다.")
        @ParameterizedTest
        @ValueSource(longs = {1, 100, 1000, 1000000000, Long.MAX_VALUE})
        void returnFixedCoupon_WhenValidValuesProvided(Long validValue) {
            // given
            String type = "FIXED";

            // when
            CouponEntity coupon = new CouponCommand.Admin.Create(type, validValue).toEntity();

            // then
            assertNotNull(coupon);
            assertNotNull(coupon.getId());
            assertEquals(CouponEntity.Type.FIXED, coupon.getType());
            assertEquals(validValue, coupon.getValue());
        }

        @DisplayName("정률 - 정상적인 값들이 제공될 때, 쿠폰이 생성된다.")
        @ParameterizedTest
        @ValueSource(longs = {1, 50, 100})
        void returnPercentageCoupon_WhenValidValuesProvided(Long validValue) {
            // given
            String type = "PERCENTAGE";

            // when
            CouponEntity coupon = new CouponCommand.Admin.Create(type, validValue).toEntity();

            // then
            assertNotNull(coupon);
            assertNotNull(coupon.getId());
            assertEquals(CouponEntity.Type.PERCENTAGE, coupon.getType());
            assertEquals(validValue, coupon.getValue());
        }

        @DisplayName("정률 - 100% 초과이면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {101, 200, 1000, Long.MAX_VALUE})
        void failsToCreatePercentageCoupon_whenValueIsOver100(Long invalidValue) {
            // given
            String type = "PERCENTAGE";

            // when
            CoreException exception = assertThrows(CoreException.class, () ->
                new CouponCommand.Admin.Create(type, invalidValue).toEntity()
            );

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

    }
}
