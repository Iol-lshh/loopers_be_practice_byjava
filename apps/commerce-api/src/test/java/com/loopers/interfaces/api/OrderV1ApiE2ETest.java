package com.loopers.interfaces.api;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMapAdapter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderV1ApiE2ETest {

    private static final Function<String, String> ENDPOINT = (subRoute) -> "/api/v1/orders" + subRoute;
    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final BrandService brandService;
    private final ProductService productService;
    private final UserService userService;
    private final PointService pointService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderFacade orderFacade;

    private BrandEntity prepareBrand() {
        BrandEntity brand = brandService.create("Test Brand");
        assertNotNull(brand.getId());
        return brand;
    }

    @Autowired
    public OrderV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp, BrandService brandService, ProductService productService, UserService userService, PointService pointService) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.brandService = brandService;
        this.productService = productService;
        this.userService = userService;
        this.pointService = pointService;
    }

    private ProductEntity prepareProduct(String productName) {
        BrandEntity brand = prepareBrand();
        ProductCommand.Register beforeRegister = new ProductCommand.Register(
                productName,
                brand.getId(),
                10000L,
                1000L
        );
        var result = productService.register(beforeRegister);
        assertNotNull(result.getId());
        return result;
    }
    private UserEntity prepareUser(String loginId) {
        var prepareUserCommand = UserCommand.Create.of(loginId, "남", "1993-04-05", "test@gmail.com");
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        return user;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/orders")
    @Nested
    class CreateOrder {
        @DisplayName("주문 생성이 성공할 경우, 생성된 주문 정보를 응답으로 반환한다.")
        @Test
        void returnsCreatedOrder_whenOrderCreationIsSuccessful() {
            // given
            String requestUrl = ENDPOINT.apply("");
            UserEntity user = prepareUser("testUser");
            ProductEntity product = prepareProduct("Test Product");
            productService.release(product.getId());
            assertNotNull(user.getId());
            assertNotNull(product.getId());
            pointService.charge(user.getId(), 100000000L);

            var request = new OrderV1Dto.Request.Order(
                    user.getId(),
                    List.of(new OrderV1Dto.Request.Item(product.getId(), 2L))
            );

            // when
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Summary>> responseType =
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
            assertEquals(user.getId(), response.getBody().data().userId());
            assertEquals(product.getPrice()*2, response.getBody().data().totalPrice());
        }
    }

    @DisplayName("GET /api/v1/orders")
    @Nested
    class GetOrders {
        @DisplayName("유저에게 존재하는 주문이 있을 때, 주문 목록을 반환한다.")
        @Test
        void returnsOrderInfo_whenValidIdIsProvided() {
            // given
            String requestUrl = ENDPOINT.apply("");
            UserEntity user = prepareUser("testUser");
            ProductEntity product = prepareProduct("Test Product");
            productService.release(product.getId());
            assertNotNull(user.getId());
            assertNotNull(product.getId());
            pointService.charge(user.getId(), 100000000L);

            OrderCriteria.Order command = new OrderCriteria.Order(
                    user.getId(),
                    List.of(new OrderCriteria.Item(product.getId(), 2L))
            );
            var order = orderFacade.orderByPoint(command);
            assertTrue(orderService.find(order.orderId()).isPresent());

            // when
            var headers = new MultiValueMapAdapter<>(Map.of("X-User-ID", List.of(user.getId().toString())));
            ParameterizedTypeReference<ApiResponse<List<OrderV1Dto.Response.Summary>>> responseType =
                    new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(1, response.getBody().data().size());
            assertEquals(user.getId(), response.getBody().data().getFirst().userId());
        }
    }

    @DisplayName("GET /api/v1/orders/{orderId}")
    @Nested
    class GetOrder {
        @DisplayName("존재하는 유저 ID를 주면, 해당 유저의 모든 주문 정보를 반환한다.")
        @Test
        void returnsUserOrders_whenValidUserIdIsProvided() {
            // given
            String requestUrl = ENDPOINT.apply("");
            UserEntity user = prepareUser("testUser");
            ProductEntity product = prepareProduct("Test Product");
            productService.release(product.getId());
            ProductEntity product2 = prepareProduct("Test Product2");
            productService.release(product2.getId());
            assertNotNull(user.getId());
            assertNotNull(product.getId());
            pointService.charge(user.getId(), 100000000L);
            OrderCriteria.Order command = new OrderCriteria.Order(
                    user.getId(),
                    List.of(
                            new OrderCriteria.Item(product.getId(), 2L),
                            new OrderCriteria.Item(product2.getId(), 1L))
            );
            var order = orderFacade.orderByPoint(command);
            assertTrue(orderService.find(order.orderId()).isPresent());
            String orderId = order.orderId().toString();
            String requestUrlWithOrderId = requestUrl + "/" + orderId;

            // when
            var headers = new MultiValueMapAdapter<>(Map.of("X-User-ID", List.of(user.getId().toString())));
            ParameterizedTypeReference<ApiResponse<OrderV1Dto.Response.Detail>> responseType =
                    new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrlWithOrderId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(orderId, response.getBody().data().orderId().toString());
            assertEquals(user.getId(), response.getBody().data().userId());
            assertEquals(2, response.getBody().data().items().size());
            assertEquals(product.getId(), response.getBody().data().items().getFirst().productId());
        }
    }
}
