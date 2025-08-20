package com.loopers.interfaces.api;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.payment.PaymentV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

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
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PaymentFacade paymentFacade;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private static final Function<String, String> ENDPOINT = (subRoute) -> "/api/v1/payments" + subRoute;

    private final Logger logger = LoggerFactory.getLogger(PaymentV1ApiE2ETest.class);

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

    @DisplayName("POST /api/v1/payments/point")
    @Nested
    public class Point{
        @DisplayName("결제 요청 시, 결제 정보가 반환된다.")
        @Test
        void returnsPaymentInfo_whenPaymentRequest() {
            // arrange
            String requestUrl = ENDPOINT.apply("/point");
            UserEntity user = prepareUser(100000L);
            ProductEntity product = prepareProduct(10000L, 1000L);
            OrderResult.Summary order = prepareOrderByPoint(user, Map.of(product, 10L));
            assertTrue(orderService.find(order.orderId()).isPresent());

            when(paymentFacade.pay(any(PaymentCriteria.Point.class)))
                    .thenReturn(new PaymentResult.Summary(
                            order.orderId(),
                            user.getId(),
                            100000L,
                            "POINT",
                            "SUCCESS"
                    ));

            // when
            var request = new PaymentV1Dto.Request.Pay(
                    user.getId(),
                    order.orderId(),
                    "POINT"
            );
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.Response.Summary>> responseType =
                    new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    responseType
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
        }
    }

    @DisplayName("POST /api/v1/payments/transaction")
    @Nested
    public class Transaction{
        @DisplayName("결제 요청 시, 결제 정보가 반환된다.")
        @Test
        void returnsPaymentInfo_whenPaymentRequest() {
            // arrange
            String requestUrl = ENDPOINT.apply("/transaction");
            UserEntity user = prepareUser(100000L);
            ProductEntity product = prepareProduct(10000L, 1000L);
            OrderResult.Summary paymentOrder = prepareOrderByPg(user, Map.of(product, 10L));

            when(paymentFacade.pay(any(PaymentCriteria.Transaction.class)))
                    .thenReturn(new PaymentResult.Summary(
                            1L, // transactionKey
                            1L, // orderId
                            10000L,
                            "PG",
                            "SUCCESS"
                    ));

            // when
            var request = new PaymentV1Dto.Request.Transaction(
                    "paymentOrder.getOrderKey()",
                    "paymentOrder.getTransactions().getFirst().getTransactionKey()",
                    "SAMSUNG",
                    "1234-5678-9012-3456",
                    "100000",
                    "SUCCESS",
                    "결제 성공"
            );
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.Response.Summary>> responseType =
                    new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    responseType
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
        }
    }

}
