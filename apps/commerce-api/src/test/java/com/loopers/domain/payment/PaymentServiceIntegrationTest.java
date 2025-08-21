package com.loopers.domain.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private UserService userService;
    @Autowired
    private PointService pointService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderFacade orderFacade;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PaymentGateway mockPaymentGateway;

    private UserEntity prepareUser() {
        String loginId = "user" + Instancio.create(Integer.class);
        String gender = "남";
        String birthDate = "1993-04-05";
        String email = "test" + Instancio.create(Integer.class) + "@gmail.com";

        var prepareUserCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        pointService.charge(user.getId(), 10000L);
        return user;
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
    private ProductEntity prepareProduct(long price, long stock) {
        Long brandId = brandService.create("Test Brand").getId();
        assertTrue(brandService.find(brandId).isPresent());
        var productCommand = new ProductCommand.Register("Test Product", brandId, price, stock);
        ProductEntity product = productService.register(productCommand);
        assertTrue(productService.find(product.getId()).isPresent());
        productService.release(product.getId());
        return product;
    }
    private OrderResult.Summary prepareOrderByPg(UserEntity user, Map<ProductEntity, Long> products) {
        List<OrderCriteria.Item> items = products.entrySet().stream()
                .map(entry -> new OrderCriteria.Item(entry.getKey().getId(), entry.getValue()))
                .toList();
        OrderCriteria.Order criteria = new OrderCriteria.Order(user.getId(), "PG", items, List.of());
        return orderFacade.order(criteria);
    }

    @DisplayName("결제 요청")
    @Nested
    public class Request {
        @DisplayName("결제 요청 시, PG 클라이언트에 요청을 보낸다.")
        @Test
        void requestPgClient_whenRequestPayment() {
            // given
            UserEntity user = prepareUser();
            prepareUserCard(user.getId());
            ProductEntity product = prepareProduct(10000L, 10L);
            OrderResult.Summary order = prepareOrderByPg(user, Map.of(product, 1L));
            when(mockPaymentGateway.request(any(PaymentStatement.Request.class)))
                    .thenReturn(new PaymentInfo.Transaction(
                            "transactionKey1234",
                            "PENDING",
                            "결제 대기중"
                    ));

            // when
            paymentService.request(new PaymentCommand.Request(user.getId(), order.orderId(), order.totalPrice()));

            // then
            verify(mockPaymentGateway, atLeastOnce()).request(any(PaymentStatement.Request.class));
        }
    }

    @DisplayName("결제 처리 저장")
    @Nested
    public class Pay{
        @DisplayName("결제 처리시, PG 클라이언트에 상태를 조회하고, 성공시 저장한다.")
        @Test
        void savePayment_whenPgStateSuccess() {
            // given
            UserEntity user = prepareUser();
            ProductEntity product = prepareProduct(10000L, 10L);
            OrderResult.Summary order = prepareOrderByPg(user, Map.of(product, 1L));
            PaymentEntity paymentOrder = paymentService.findByOrderId(order.orderId()).get();
            PaymentInfo.Order orderInfo = new PaymentInfo.Order(
                    paymentOrder.getOrderKey(),
                    List.of(new PaymentInfo.Transaction(
                            "transactionKey1234",
                            PaymentEntity.State.SUCCESS.name(),
                            "정상 승인되었습니다."
                    ))
            );
            when(mockPaymentGateway.findOrder(anyLong(), anyString()))
                    .thenReturn(Optional.of(orderInfo));

            // when
            PaymentCommand.Transaction transaction = new PaymentCommand.Transaction(
                    user.getId(),
                    order.orderId(),
                    paymentOrder.getOrderKey(),
                    "transactionKey1234",
                    "SAMSUNG",
                    "1234-5678-9012-3456",
                    "10000",
                    "SUCCESS",
                    "정상 승인되었습니다."
            );
            PaymentEntity paymentEntity = paymentService.pay(transaction);

            // then
            assertNotNull(paymentEntity);
            verify(mockPaymentGateway, atLeastOnce()).findOrder(anyLong(), anyString());
        }
    }
}
