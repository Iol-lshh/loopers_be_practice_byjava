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
            OrderCommand.Order command = new OrderCommand.Order(1L, List.of(), List.of());

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("주문 아이템이 null 이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateOrder_whenItemsAreNull() {
            // given
            OrderCommand.Order command = new OrderCommand.Order(1L, null, List.of());

            // when
            CoreException exception = assertThrows(CoreException.class, () -> OrderEntity.from(command));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }

}
