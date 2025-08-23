package com.loopers.domain.order;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private OrderService orderService;
    @MockitoSpyBean
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PointService pointService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


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

    @DisplayName("주문 완료 상태 저장")
    @Nested
    class Complete {
        @DisplayName("주문이 완료 상태로 변경되면, 주문 상태가 '완료'로 업데이트된다.")
        @Test
        void completeOrder_updatesStatusToCompleted() {
            prepareUser(10000L);
            prepareProduct(100L, 1L);
            // given
            OrderCommand.Order orderCommand = new OrderCommand.Order(
                    1L,
                    "POINT",
                    List.of(new OrderCommand.Item(1L, 100L, 100L)),
                    List.of()
            );
            OrderEntity order = orderService.register(orderCommand);

            // when
            OrderCommand.Complete payCommand = new OrderCommand.Complete(
                    order.getUserId(),
                    order.getId(),
                    order.getTotalPrice(),
                    "POINT"
            );
            OrderEntity completedOrder = orderService.complete(payCommand);

            // then
            assertNotNull(completedOrder);
            assertEquals(OrderEntity.State.COMPLETED, completedOrder.getState());
            var monitor = orderService.find(completedOrder.getId()).orElseThrow();
            assertEquals(OrderEntity.State.COMPLETED, monitor.getState());
        }

        @DisplayName("동시에 여러 주문 완료 요청이 들어올 때, 한번만 주문의 상태가 변경된다.")
        @Test
        void completeOrder_concurrencyTest() throws InterruptedException {
            // given
            prepareUser(10000L);
            prepareProduct(100L, 1L);
            OrderCommand.Order orderCommand = new OrderCommand.Order(
                    1L,
                    "POINT",
                    List.of(new OrderCommand.Item(1L, 100L, 100L)),
                    List.of()
            );
            OrderEntity order = orderService.register(orderCommand);
            long orderId = order.getId();

            // when
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            AtomicInteger completedCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        OrderCommand.Complete payCommand = new OrderCommand.Complete(
                                order.getUserId(),
                                orderId,
                                order.getTotalPrice(),
                                "POINT"
                        );
                        orderService.complete(payCommand);
                        completedCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertEquals(1, completedCount.get());
            verify(orderRepository, atLeastOnce()).save(any(OrderEntity.class));
        }
    }
}
