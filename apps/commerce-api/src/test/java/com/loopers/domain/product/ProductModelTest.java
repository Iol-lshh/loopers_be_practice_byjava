package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ProductModelTest {

    @DisplayName("생성")
    @Nested
    class Create {
        @DisplayName("상품 이름이 비어있으면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        void failsToCreateProduct_whenNameIsEmpty(String invalidProductName) {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand =new ProductCommand.Register(
                        invalidProductName,
                        1L, // brandId
                        1000L, // price
                        10L // stock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("상품 이름이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateProduct_whenNameIsNull() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand = new ProductCommand.Register(
                        null,
                        1L, // brandId
                        1000L, // price
                        10L // stock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("상품 브랜드 ID가 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateProduct_whenBrandIdIsNull() {
            // given
            Long invalidBrandId = null;

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand = new ProductCommand.Register(
                        "Valid Product Name",
                        invalidBrandId,
                        1000L, // price
                        10L // stock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("상품 가격이 음수이면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {-1, -100})
        void failsToCreateProduct_whenPriceIsNullOrZeroOrNegative(Long invalidPrice) {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand = new ProductCommand.Register(
                        "Valid Product Name",
                        1L, // brandId
                        invalidPrice,
                        10L // stock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("상품 가격이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateProduct_whenPriceIsNull() {
            // given
            Long invalidPrice = null;

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand = new ProductCommand.Register(
                        "Valid Product Name",
                        1L, // brandId
                        invalidPrice,
                        10L // stock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("상품 재고가 음수이면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {-1, -100})
        void failsToCreateProduct_whenStockIsNullOrZeroOrNegative(Long invalidStock) {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand = new ProductCommand.Register(
                        "Valid Product Name",
                        1L, // brandId
                        1000L, // price
                        invalidStock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("상품 재고가 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToCreateProduct_whenStockIsNull() {
            // given
            Long invalidStock = null;

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Register invalidCommand = new ProductCommand.Register(
                        "Valid Product Name",
                        1L, // brandId
                        1000L, // price
                        invalidStock
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }

    @DisplayName("재고 감소")
    @Nested
    class DecreaseStock {
        @DisplayName("재고 감소 수량이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToDecreaseStock_whenQuantityIsNull() {
            // given
            ProductEntity product = new ProductEntity("Valid Product Name", 1L, 1000L, 10L);
            product.release();

            // when
            CoreException exception = assertThrows(CoreException.class, () -> product.deductStock(null));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("재고 감소 수량이 0 이하이면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        void failsToDecreaseStock_whenQuantityIsZeroOrNegative(long invalidQuantity) {
            // given
            ProductEntity product = new ProductEntity("Valid Product Name", 1L, 1000L, 10L);
            product.release();

            // when
            CoreException exception = assertThrows(CoreException.class, () -> product.deductStock(invalidQuantity));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("재고가 부족한 경우 재고 감소 시도시, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToDecreaseStock_whenInsufficientStock() {
            // given
            ProductEntity product = new ProductEntity("Valid Product Name", 1L, 1000L, 10L);
            product.release();

            // when
            CoreException exception = assertThrows(CoreException.class, () -> product.deductStock(11L));

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("유효한 재고 감소 수량이면 성공적으로 재고가 감소한다.")
        @Test
        void succeedsToDecreaseStock_whenValidQuantity() {
            // given
            ProductEntity product = new ProductEntity("Valid Product Name", 1L, 1000L, 10L);
            product.release();

            // when
            product.deductStock(5L);

            // then
            assertEquals(5L, product.getStock());
        }
    }

    @DisplayName("상품 출시")
    @Nested
    class ReleaseProduct {
        @DisplayName("재고가 부족한 경우 출시 시도시, BAD_REQUEST 예외를 던진다.")
        @Test
        void failsToReleaseProduct_whenInsufficientStock() {
            // given
            ProductEntity product = new ProductEntity("Valid Product Name", 1L, 1000L, 0L);

            // when
            CoreException exception = assertThrows(CoreException.class, product::release);

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("재고가 충분한 경우 성공적으로 출시된다.")
        @Test
        void succeedsToReleaseProduct_whenSufficientStock() {
            // given
            ProductEntity product = new ProductEntity("Valid Product Name", 1L, 1000L, 10L);

            // when
            product.release();

            // then
            assertEquals(ProductEntity.State.StateType.OPEN, product.getState().getValue());
        }
    }
}
