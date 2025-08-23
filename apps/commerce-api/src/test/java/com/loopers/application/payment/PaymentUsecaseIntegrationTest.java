package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.PointService;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.payment.PgV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Optional;

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
    @Autowired
    private PaymentService paymentService;

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

    @MockitoBean
    private PaymentGateway mockPaymentGateway;

    private static final Logger log = LoggerFactory.getLogger(PaymentUsecaseIntegrationTest.class);

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
    private OrderResult.Summary prepareOrderByPoint(UserEntity user, Map<ProductEntity, Long> products) {
        List<OrderCriteria.Item> items = products.entrySet().stream()
                .map(entry -> new OrderCriteria.Item(entry.getKey().getId(), entry.getValue()))
                .toList();
        OrderCriteria.Order criteria = new OrderCriteria.Order(user.getId(), "POINT", items, List.of());
        return orderFacade.order(criteria);
    }
    private OrderResult.Summary prepareOrderByPg(UserEntity user, Map<ProductEntity, Long> products) {
        List<OrderCriteria.Item> items = products.entrySet().stream()
                .map(entry -> new OrderCriteria.Item(entry.getKey().getId(), entry.getValue()))
                .toList();
        OrderCriteria.Order criteria = new OrderCriteria.Order(user.getId(), "PG", items, List.of());
        return orderFacade.order(criteria);
    }
    private UserCardEntity prepareUserCard(Long userId) {
        UserCardCommand.Register userCardEntity = new UserCardCommand.Register(
                userId,
                "1234-5678-9012-3456",
                "SAMSUNG"
        );
        UserCardEntity card = paymentService.registerCard(userCardEntity);
        assertTrue(userService.find(userId).isPresent());
        return card;
    }

    @Nested
    @DisplayName("포인트 결제 처리")
    class Point {
        @DisplayName("PENDING 상태의 주문에 대해, 재고 차감이 가능하고, 포인트 결제 가능한 주문의 유저가 포인트 결제 요청 시, 결제 정보가 반환된다.")
        @Test
        void returnPaymentInfo_whenPay() {
            // given
            UserEntity user = prepareUser(10000L);
            ProductEntity product = prepareProduct(1000L, 10L);
            OrderResult.Summary order = prepareOrderByPoint(user, Map.of(product, 10L));

            // when
            var criteria = new PaymentCriteria.Point(
                    user.getId(),
                    order.orderId(),
                    "POINT"
            );
            PaymentResult.Summary result = paymentFacade.pay(criteria);

            // then
            assertEquals("COMPLETED", result.state());
            verify(pointService, times(1)).pay(anyLong(), anyLong());
            verify(productService, times(1)).deduct(anyMap());
            verify(orderService, times(1)).complete(any(OrderCommand.Complete.class));
        }

        @DisplayName("결제 요청 시, 주문이 존재하지 않으면 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwNotFoundException_whenOrderNotFound() {
            // given
            UserEntity user = prepareUser(10000L);
            long nonExistentOrderId = 9999L;
            assertTrue(orderService.find(nonExistentOrderId).isEmpty());

            // when
            var criteria = new PaymentCriteria.Point(
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
            OrderResult.Summary order = prepareOrderByPoint(prepareUser(10000L), Map.of(product, 10L));

            // when
            var criteria = new PaymentCriteria.Point(
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
            OrderResult.Summary order = prepareOrderByPoint(user, Map.of(product, 5L));

            UserEntity user2 = prepareUser(10000L);
            OrderResult.Summary order2 = prepareOrderByPoint(user, Map.of(product, 1L));
            paymentFacade.pay(new PaymentCriteria.Point(
                    user2.getId(),
                    order2.orderId(),
                    "POINT"
            ));
            assertEquals(4L, productService.find(product.getId()).orElseThrow().getStock());

            // when
            var criteria = new PaymentCriteria.Point(
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
            OrderResult.Summary order = prepareOrderByPoint(user, Map.of(product, 1L));

            // when
            var criteria = new PaymentCriteria.Point(
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
    @DisplayName("포인트 동시성 결제 처리")
    class ConcurrentPoint {
        @DisplayName("동시에 같은 주문에 대해 결제를 시도할 때, 낙관적 락으로 인해 OptimisticLockException이 발생한다.")
        @Test
        void throwOptimisticLockException_whenConcurrentPayment() throws InterruptedException {
            // given
            UserEntity user = prepareUser(100000L);
            ProductEntity product = prepareProduct(1000L, 100L);
            OrderResult.Summary orderResult = prepareOrderByPoint(user, Map.of(product, 1L));

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
                        var criteria = new PaymentCriteria.Point(
                                user.getId(),
                                orderResult.orderId(),
                                "POINT"
                        );
                        paymentFacade.pay(criteria);
                        successCount.incrementAndGet();
                    } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                        exceptionCount.incrementAndGet();
                        log.info("OptimisticLockException 발생: " + e.getMessage());
                    } catch (Exception e) {
                        log.info("다른 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // 모든 스레드 동시 시작
            endLatch.await();
            executorService.shutdown();

            // then
            log.info("성공한 결제 수: " + successCount.get());
            log.info("낙관적 락 예외 수: " + exceptionCount.get());
            
            // 최소 하나는 성공하고, 최소 하나는 실패해야 함
            assertTrue(successCount.get() >= 1, "최소 하나의 결제는 성공해야 합니다.");
            assertTrue(exceptionCount.get() >= 1, "최소 하나의 OptimisticLockException이 발생해야 합니다.");
        }
    }

    @Nested
    @DisplayName("PG 결제 처리")
    class Transaction {
        @DisplayName("PENDING 상태의 주문에 대해, 재고 차감이 가능하고, PG 결제 가능한 주문의 유저가 PG 결제 요청 시, 결제 정보가 반환된다.")
        @Test
        void returnPaymentInfo_whenPay() {
            // given
            UserEntity user = prepareUser(10000L);
            prepareUserCard(user.getId());
            ProductEntity product = prepareProduct(1000L, 10L);
            when(mockPaymentGateway.request(any(PaymentStatement.Request.class)))
                    .thenReturn(new PgV1Dto.Response.Transaction(
                            "orderKey1234",
                            "PENDING",
                            "결제 대기중"
                    ).getInfo());
            
            // PaymentGateway.findOrder() 메서드에 대한 Mock 설정 추가
            when(mockPaymentGateway.findOrder(anyLong(), anyString()))
                    .thenReturn(Optional.of(new PaymentInfo.Order(
                            "1",
                            List.of(new PaymentInfo.Transaction(
                                    "orderKey1234",
                                    "PENDING",
                                    "결제 대기중"
                            ))
                    )));
            
            OrderResult.Summary order = prepareOrderByPg(user, Map.of(product, 10L));
            PaymentEntity paymentOrder = paymentService.findByOrderId(order.orderId()).get();
            assertNotNull(paymentOrder);

            // when
            var criteria = new PaymentCriteria.Transaction(
                    "orderKey1234",
                    paymentOrder.getOrderKey(),  // orderKey 대신 orderId 사용
                    "SAMSUNG",
                    "1234-5678-9012-3456",
                    "10000",
                    "PENDING",
                    "결제 대기중"
            );
            PaymentResult.Summary result = paymentFacade.pay(criteria);

            // then
            assertEquals("COMPLETED", result.state());
            verify(productService, times(1)).deduct(anyMap());
            verify(orderService, times(1)).register(any(OrderCommand.Order.class));
        }

        @DisplayName("결제 요청이 PG로 부터 실패되었을 때, 주문이 취소된다.")
        @Test
        void cancelOrder_whenPaymentFailed() throws InterruptedException {
            // given
            UserEntity user = prepareUser(10000L);
            prepareUserCard(user.getId());
            ProductEntity product = prepareProduct(1000L, 10L);
            when(mockPaymentGateway.request(any(PaymentStatement.Request.class)))
                    .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR));

            OrderCriteria.Order criteria = new OrderCriteria.Order(user.getId(), "PG", List.of(new OrderCriteria.Item(product.getId(), 1L)), List.of());

            //when
            orderFacade.order(criteria);

            // then
            Thread.sleep(1000); // PG 요청이 비동기로 처리되므로 잠시 대기
            var orderList = orderFacade.list(user.getId());
            assertEquals(1, orderList.size());
            assertEquals("CANCELLED", orderList.getFirst().state());
        }
    }
}
