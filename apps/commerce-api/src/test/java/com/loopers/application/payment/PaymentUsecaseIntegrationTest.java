package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.point.PointService;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class PaymentUsecaseIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private UserService userService;
    @Autowired
    private BrandService brandService;
    @MockitoSpyBean
    private ProductService productService;
    @MockitoSpyBean
    private OrderService orderService;
    @MockitoSpyBean
    private PointService pointService;
    @Autowired
    private PaymentFacade paymentFacade;

    private UserEntity prepareUser(Long point) {
        String loginId = "user" + Instancio.create(Integer.class);
        String gender = "남";
        String birthDate = "1993-04-05";
        String email = "test" + Instancio.create(Integer.class) + "@gmail.com";

        var prepareUserCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        pointService.charge(user.getId(), point);
        return user;
    }
    private ProductEntity prepareProduct(long price, long stock) {
        Long brandId = brandService.create("Test Brand").getId();
        assertTrue(brandService.find(brandId).isPresent());
        var productCommand = new ProductCommand.Register("Test Product", brandId, price, stock);
        ProductEntity product = productService.register(productCommand);
        assertTrue(productService.find(product.getId()).isPresent());
        productService.release(product.getId());
        return product;
    }
    private OrderResult.Summary prepareOrder(UserEntity user, Map<ProductEntity, Long> products) {
        List<OrderCriteria.Item> items = products.entrySet().stream()
                .map(entry -> new OrderCriteria.Item(entry.getKey().getId(), entry.getValue()))
                .toList();
        OrderCriteria.Order criteria = new OrderCriteria.Order(user.getId(), items);
        return orderFacade.order(criteria);
    }

    @Nested
    @DisplayName("결제 처리")
    class Pay {
        @DisplayName("PENDING 상태의 주문에 대해, 재고 차감이 가능하고, 포인트 결제 가능한 주문의 유저가 포인트 결제 요청 시, 결제 정보가 반환된다.")
        @Test
        void returnPaymentInfo_whenPay() {
            // given
            UserEntity user = prepareUser(10000L);
            ProductEntity product = prepareProduct(1000L, 10L);
            OrderResult.Summary order = prepareOrder(user, Map.of(product, 10L));

            // when
            var criteria = new PaymentCriteria.Pay(
                    user.getId(),
                    order.orderId(),
                    "POINT"
            );
            PaymentResult.Summary result = paymentFacade.pay(criteria);

            // then
            assertNotNull(result.paymentId());
            verify(pointService, times(1)).pay(anyLong(), anyLong());
            verify(productService, times(1)).deduct(anyMap());
            verify(orderService, times(1)).complete(anyLong());
        }

        @DisplayName("결제 요청 시, 주문이 존재하지 않으면 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwNotFoundException_whenOrderNotFound() {
            // given
            UserEntity user = prepareUser(10000L);
            long nonExistentOrderId = 9999L;
            assertTrue(orderService.find(nonExistentOrderId).isEmpty());

            // when
            var criteria = new PaymentCriteria.Pay(
                    user.getId(),
                    nonExistentOrderId,
                    "POINT"
            );
            var result = assertThrows(CoreException.class, () -> paymentFacade.pay(criteria));

            // then
            assertEquals(ErrorType.NOT_FOUND ,result.getErrorType());
        }

        @DisplayName("결제 요청 시, 유저가 존재하지 않으면 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwNotFoundException_whenUserNotFound() {
            // given
            long nonExistentUserId = 9999L;
            assertTrue(userService.find(nonExistentUserId).isEmpty());
            ProductEntity product = prepareProduct(1000L, 10L);
            OrderResult.Summary order = prepareOrder(prepareUser(10000L), Map.of(product, 10L));

            // when
            var criteria = new PaymentCriteria.Pay(
                    nonExistentUserId,
                    order.orderId(),
                    "POINT"
            );
            var result = assertThrows(CoreException.class, () -> paymentFacade.pay(criteria));

            // then
            assertEquals(ErrorType.NOT_FOUND ,result.getErrorType());
        }

        @DisplayName("결제 요청 시, 재고가 부족하면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwOutOfStockException_whenInsufficientStock() {
            // given
            UserEntity user = prepareUser(10000L);
            ProductEntity product = prepareProduct(1000L, 5L);
            OrderResult.Summary order = prepareOrder(user, Map.of(product, 5L));

            UserEntity user2 = prepareUser(10000L);
            OrderResult.Summary order2 = prepareOrder(user, Map.of(product, 1L));
            paymentFacade.pay(new PaymentCriteria.Pay(
                    user2.getId(),
                    order2.orderId(),
                    "POINT"
            ));
            assertEquals(4L, productService.find(product.getId()).orElseThrow().getStock());

            // when
            var criteria = new PaymentCriteria.Pay(
                    user.getId(),
                    order.orderId(),
                    "POINT"
            );
            var result = assertThrows(CoreException.class, () -> paymentFacade.pay(criteria));

            // then
            assertEquals(ErrorType.BAD_REQUEST ,result.getErrorType());
        }

        @DisplayName("결제 요청 시, 유저의 포인트가 부족하면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwInsufficientPointException_whenUserHasInsufficientPoint() {
            // given
            UserEntity user = prepareUser(500L); // 유저의 포인트를 500으로 설정
            ProductEntity product = prepareProduct(1000L, 10L);
            OrderResult.Summary order = prepareOrder(user, Map.of(product, 1L));

            // when
            var criteria = new PaymentCriteria.Pay(
                    user.getId(),
                    order.orderId(),
                    "POINT"
            );
            var result = assertThrows(CoreException.class, () -> paymentFacade.pay(criteria));

            // then
            assertEquals(ErrorType.BAD_REQUEST ,result.getErrorType());
        }
    }
}
