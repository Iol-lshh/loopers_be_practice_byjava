package com.loopers.domain.order;

import com.loopers.utils.DatabaseCleanUp;
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

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 완료 상태 저장")
    @Nested
    class Complete {
        @DisplayName("주문이 완료 상태로 변경되면, 주문 상태가 '완료'로 업데이트된다.")
        @Test
        void completeOrder_updatesStatusToCompleted() {
            // given
            OrderCommand.Order orderCommand = new OrderCommand.Order(
                    1L,
                    List.of(new OrderCommand.Item(1L, 100L, 100L)),
                    List.of()
            );
            OrderEntity order = orderService.register(orderCommand);

            // when
            OrderEntity completedOrder = orderService.complete(order.getId());

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
            OrderCommand.Order orderCommand = new OrderCommand.Order(
                    1L,
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
                        orderService.complete(orderId);
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
