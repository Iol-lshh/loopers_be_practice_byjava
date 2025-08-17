package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderModelTest {
    @DisplayName("주문 생성")
    @Nested
    class Create {
        @DisplayName("주문 아이템이 비어있으면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateOrder_whenItemsAreEmpty() {
            // given
            OrderCommand.Order command = new OrderCommand.Order(1L, "POINT", List.of(), List.of());

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("주문 아이템이 null 이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateOrder_whenItemsAreNull() {
            // given
            OrderCommand.Order command = new OrderCommand.Order(1L, "POINT", null, List.of());

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }

    @DisplayName("주문 가격")
    @Nested
    class GetTotalPrice{
        @DisplayName("주문 아이템의 가격을 합산하여 총 가격을 반환한다.")
        @Test
        void returnsTotalPriceOfOrderItems() {
            // given
            OrderCommand.Item item1 = new OrderCommand.Item(1L, 1000L, 2L);
            OrderCommand.Order command = new OrderCommand.Order(1L, "POINT", List.of(item1), List.of());
            OrderEntity order = OrderEntity.from(command);

            // when
            Long totalPrice = order.getTotalPrice();

            // then
            assertEquals(2000L, totalPrice);
        }

        @DisplayName("쿠폰 할인 가격이 총 가격보다 크면, 0을 반환한다.")
        @Test
        void returnsZero_whenCouponDiscountExceedsTotalPrice() {
            // given
            OrderCommand.Item item1 = new OrderCommand.Item(1L, 1000L, 2L);
            OrderCommand.Coupon coupon = new OrderCommand.Coupon(1L, 5000L);
            OrderCommand.Order command = new OrderCommand.Order(1L, "POINT", List.of(item1), List.of(coupon));
            OrderEntity order = OrderEntity.from(command);

            // when
            Long totalPrice = order.getTotalPrice();

            // then
            assertEquals(0L, totalPrice);
        }
    }

}
