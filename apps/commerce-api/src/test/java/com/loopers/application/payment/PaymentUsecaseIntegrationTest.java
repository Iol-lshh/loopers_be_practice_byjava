package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.OrderRepository;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PaymentUsecaseIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CouponRepository couponRepository;

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
        OrderCriteria.Order criteria = new OrderCriteria.Order(user.getId(), items, List.of());
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

    @Nested
    @DisplayName("동시성 결제 처리")
    class ConcurrentPayment {
        @DisplayName("동시에 같은 주문에 대해 결제를 시도할 때, 낙관적 락으로 인해 OptimisticLockException이 발생한다.")
        @Test
        void throwOptimisticLockException_whenConcurrentPayment() throws InterruptedException {
            // given
            UserEntity user = prepareUser(100000L);
            ProductEntity product = prepareProduct(1000L, 100L);
            OrderResult.Summary orderResult = prepareOrder(user, Map.of(product, 1L));

            // when
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(2);
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            AtomicInteger exceptionCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);

            // 두 스레드가 정확히 동시에 시작하도록 설정
            for (int i = 0; i < 2; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                        var criteria = new PaymentCriteria.Pay(
                                user.getId(),
                                orderResult.orderId(),
                                "POINT"
                        );
                        paymentFacade.pay(criteria);
                        successCount.incrementAndGet();
                    } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                        exceptionCount.incrementAndGet();
                        System.out.println("OptimisticLockException 발생: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("다른 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // 모든 스레드 동시 시작
            endLatch.await();
            executorService.shutdown();

            // then
            System.out.println("성공한 결제 수: " + successCount.get());
            System.out.println("낙관적 락 예외 수: " + exceptionCount.get());
            
            // 최소 하나는 성공하고, 최소 하나는 실패해야 함
            assertTrue(successCount.get() >= 1, "최소 하나의 결제는 성공해야 합니다.");
            assertTrue(exceptionCount.get() >= 1, "최소 하나의 OptimisticLockException이 발생해야 합니다.");
        }
    }
}
