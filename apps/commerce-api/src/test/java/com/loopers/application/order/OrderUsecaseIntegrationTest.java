package com.loopers.application.order;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderStatement;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderUsecaseIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private PointService pointService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponService couponService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private OrderService orderService;

    private UserEntity prepareUser() {
        String loginId = "user" + Instancio.create(Integer.class);
        String gender = "남";
        String birthDate = "1993-04-05";
        String email = "test" + Instancio.create(Integer.class) + "@gmail.com";
        
        var prepareUserCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        pointService.charge(user.getId(), Long.MAX_VALUE);
        return user;
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
    private ProductEntity prepareProduct() {
        Long brandId = brandService.create("Test Brand").getId();
        assertTrue(brandService.find(brandId).isPresent());
        var productCommand = new ProductCommand.Register("Test Product", brandId, 10000L, 1000L);
        ProductEntity product = productService.register(productCommand);
        assertTrue(productService.find(product.getId()).isPresent());
        return product;
    }
    private ProductEntity prepareProduct(long price, long stock) {
        Long brandId = brandService.create("Test Brand").getId();
        assertTrue(brandService.find(brandId).isPresent());
        var productCommand = new ProductCommand.Register("Test Product", brandId, price, stock);
        ProductEntity product = productService.register(productCommand);
        assertTrue(productService.find(product.getId()).isPresent());
        return product;
    }
    private ProductEntity prepareReleasedProduct() {
        ProductEntity product = prepareProduct();
        product.release();
        productRepository.save(product);
        var result = productService.find(product.getId());
        assertTrue(result.isPresent());
        assertEquals(ProductEntity.State.StateType.OPEN, result.get().getState().getValue());
        return product;
    }
    private ProductEntity prepareReleasedProduct(long price, long stock) {
        ProductEntity product = prepareProduct(price, stock);
        product.release();
        productRepository.save(product);
        var result = productService.find(product.getId());
        assertTrue(result.isPresent());
        assertEquals(ProductEntity.State.StateType.OPEN, result.get().getState().getValue());
        return product;
    }
    private OrderEntity prepareOrderByPoint(UserEntity user, ProductEntity product, Long quantity) {
        var orderCriteria = new OrderCriteria.Order(
                user.getId(),
                "POINT",
                List.of(new OrderCriteria.Item(product.getId(), quantity)),
                List.of()
        );
        var info = orderFacade.order(orderCriteria);
        assertNotNull(info.orderId());
        var orderOptional = orderService.find(info.orderId());
        assertTrue(orderOptional.isPresent());
        return orderOptional.get();
    }
    private CouponEntity prepareCoupon(CouponEntity.Type type, long value) {
        var couponCommand = new CouponCommand.Admin.Create(
                type.name(),
                value
        );
        CouponEntity coupon = couponService.register(couponCommand);
        assertTrue(couponService.find(coupon.getId()).isPresent());
        return coupon;
    }

    @DisplayName("주문 등록")
    @Nested
    class Order {
        @DisplayName("유저가 출시된 상품들로 주문시, 성공적으로 주문이 등록된다")
        @Test
        void returnsOrderInfo_whenUserExistsAndProductsAreValid() {
            // Given
            ProductEntity product = prepareReleasedProduct();
            Long quantity = 1L;
            UserEntity user = prepareUser(product.getPrice() * quantity);
            var orderCriteria = new OrderCriteria.Order(
                    user.getId(),
                    "POINT",
                    List.of(new OrderCriteria.Item(product.getId(), quantity)),
                    List.of()
            );

            // When
            OrderResult.Summary orderInfo = orderFacade.order(orderCriteria);

            // Then
            assertNotNull(orderInfo.orderId());
            assertEquals(user.getId(), orderInfo.userId());
            assertEquals(product.getPrice() * quantity, orderInfo.totalPrice());
        }

        @DisplayName("존재하지 않는 유저로 주문시, NOT_FOUND 예외가 발생한다")
        @Test
        void throwsNotFoundException_whenUserDoesNotExist() {
            // Given
            Long nonExistentUserId = 999L;
            assertTrue(userService.find(nonExistentUserId).isEmpty());
            ProductEntity product = prepareReleasedProduct();

            // When
            var result = assertThrows(CoreException.class, () -> {
                var orderCriteria = new OrderCriteria.Order(
                        nonExistentUserId,
                        "POINT",
                        List.of(new OrderCriteria.Item(product.getId(), 1L)), // 임의의 상품 ID 사용,
                        List.of()
                );
                orderFacade.order(orderCriteria);
            });

            // Then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

        @DisplayName("존재하지 않는 상품으로 주문시, NOT_FOUND 예외가 발생한다")
        @Test
        void throwsNotFoundException_whenProductDoesNotExist() {
            // Given
            Long validUserId = prepareUser().getId();
            Long nonExistentProductId = 999L;
            assertTrue(productService.find(nonExistentProductId).isEmpty());

            // When
            var result = assertThrows(CoreException.class, () -> {
                var orderCriteria = new OrderCriteria.Order(
                        validUserId,
                        "POINT",
                        List.of(new OrderCriteria.Item(nonExistentProductId, 1L)),
                        List.of()
                );
                orderFacade.order(orderCriteria);
            });

            // Then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

        @DisplayName("출시되지 않은 상품으로 주문시, BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenProductIsNotReleased() {
            // Given
            UserEntity user = prepareUser();
            ProductEntity product = prepareProduct();

            // When
            var orderCriteria = new OrderCriteria.Order(
                    user.getId(),
                    "POINT",
                    List.of(new OrderCriteria.Item(product.getId(), product.getStock() + 1)),
                    List.of()
            );
            var result = assertThrows(CoreException.class, () -> orderFacade.order(orderCriteria));

            // Then
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }

        @DisplayName("빈 상품 목록 주문시, BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenProductListIsEmpty() {
            // Given
            Long userId = prepareUser().getId();
            List<OrderCriteria.Item> emptyProductList = List.of();
            var emptyOrderCriteria = new OrderCriteria.Order(userId, "POINT", emptyProductList, List.of());

            // When
            var result = assertThrows(CoreException.class, () -> orderFacade.order(emptyOrderCriteria));

            // Then
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }

        @DisplayName("재고 부족한 상품 주문시, BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenProductStockIsInsufficient() {
            // Given
            UserEntity user = prepareUser();
            ProductEntity product = prepareReleasedProduct(10000L, 1L); // 재고가 없는 상품

            // When
            var orderCriteria = new OrderCriteria.Order(
                    user.getId(),
                    "POINT",
                    List.of(new OrderCriteria.Item(product.getId(), 2L)),
                    List.of()
            );
            var result = assertThrows(CoreException.class, () -> orderFacade.order(orderCriteria));

            // Then
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }

    }

    @DisplayName("쿠폰과 함께 주문")
    @Nested
    class OrderWithCoupon {
        @DisplayName("유저가 쿠폰을 사용하여 주문시, 성공적으로 주문이 등록된다")
        @Test
        void returnsOrderInfo_whenUserExistsAndCouponsAreValid() {
            // Given
            UserEntity user = prepareUser(10000L); // 충분한 포인트를 가진 유저
            ProductEntity product = prepareReleasedProduct(6000L, 10L);
            CouponEntity coupon = prepareCoupon(CouponEntity.Type.FIXED, 5000L);
            Long quantity = 2L;

            var orderCriteria = new OrderCriteria.Order(
                    user.getId(),
                    "POINT",
                    List.of(new OrderCriteria.Item(product.getId(), quantity)),
                    List.of(coupon.getId())
            );

            // When
            OrderResult.Summary orderInfo = orderFacade.order(orderCriteria);

            // Then
            assertNotNull(orderInfo.orderId());
            assertEquals(user.getId(), orderInfo.userId());
            assertEquals(product.getPrice() * quantity - coupon.getAppliedValue(product.getPrice() * quantity), orderInfo.totalPrice());
        }

        @DisplayName("존재하지 않는 쿠폰으로 주문시, NOT_FOUND 예외가 발생한다")
        @Test
        void throwsNotFoundException_whenCouponDoesNotExist() {
            // Given
            UserEntity user = prepareUser(10000L);
            ProductEntity product = prepareReleasedProduct(6000L, 10L);
            Long nonExistentCouponId = 999L;
            assertTrue(couponService.find(nonExistentCouponId).isEmpty());
            Long quantity = 2L;

            // When
            var result = assertThrows(CoreException.class, () -> {
                var orderCriteria = new OrderCriteria.Order(
                        user.getId(),
                        "POINT",
                        List.of(new OrderCriteria.Item(product.getId(), quantity)),
                        List.of(nonExistentCouponId)
                );
                orderFacade.order(orderCriteria);
            });

            // Then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }
    }

    @DisplayName("주문 목록 조회")
    @Nested
    class OrderList {
        @DisplayName("유저가 존재하는 경우, 주문 목록을 조회에 성공한다")
        @Test
        void returnsOrderList_whenUserExists() {
            // Given
            UserEntity preparedUser = prepareUser();
            ProductEntity preparedProduct = prepareReleasedProduct();
            OrderEntity preparedOrder = prepareOrderByPoint(preparedUser, preparedProduct, 1L);

            // When
            List<OrderResult.Summary> orderList = orderFacade.list(preparedUser.getId());

            // Then
            assertFalse(orderList.isEmpty());
            assertEquals(1, orderList.size());
            OrderResult.Summary orderInfo = orderList.getFirst();
            assertEquals(preparedOrder.getId(), orderInfo.orderId());
        }

        @DisplayName("존재하지 않는 유저로 주문 목록 조회시, NOT_FOUND 예외가 발생한다")
        @Test
        void throwsNotFoundException_whenUserDoesNotExist() {
            // Given
            Long nonExistentUserId = 999L;
            assertTrue(userService.find(nonExistentUserId).isEmpty());

            // When
            var result = assertThrows(CoreException.class, () -> orderFacade.list(nonExistentUserId));

            // Then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

        @DisplayName("주문 목록이 없는 경우, 빈 목록을 반환한다")
        @Test
        void returnsEmptyList_whenNoOrdersExist() {
            // Given
            Long validUserId = prepareUser().getId();
            assertTrue(orderService.find(OrderStatement.userId(validUserId)).isEmpty());

            // When
            List<OrderResult.Summary> orderList = orderFacade.list(validUserId);

            // Then
            assertTrue(orderList.isEmpty());
        }
    }

    @DisplayName("주문 상세 조회")
    @Nested
    class OrderDetail {
        @DisplayName("유저와 관계있는 주문을 상세 조회시, 주문 상세 정보를 반환한다")
        @Test
        void returnsOrderDetail_whenUserAndOrderExist() {
            // Given
            UserEntity user = prepareUser();
            ProductEntity product = prepareReleasedProduct();
            OrderEntity order = prepareOrderByPoint(user, product, 1L);

            // When
            OrderResult.Detail orderDetail = orderFacade.detail(user.getId(), order.getId());

            // Then
            assertNotNull(orderDetail);
            assertEquals(order.getId(), orderDetail.orderId());
        }

        @DisplayName("존재하지 않는 주문으로 주문 상세 조회시, NOT_FOUND 예외가 발생한다")
        @Test
        void throwsNotFoundException_whenOrderDoesNotExist() {
            // Given
            Long validUserId = prepareUser().getId();
            Long nonExistentOrderId = 999L;
            assertTrue(orderService.find(nonExistentOrderId).isEmpty());

            // When
            var result = assertThrows(CoreException.class, () -> orderFacade.detail(validUserId, nonExistentOrderId));

            // Then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

        @DisplayName("유저와 주문이 관계가 없는 경우, BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenUserAndOrderDoNotMatch() {
            // Given
            UserEntity user = prepareUser();
            ProductEntity product = prepareReleasedProduct();
            OrderEntity order = prepareOrderByPoint(user, product, 1L);
            UserEntity otherUser = prepareUser(); // 다른 유저를 준비

            // When
            var result = assertThrows(CoreException.class, () -> orderFacade.detail(otherUser.getId(), order.getId()));

            // Then
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
        }
    }
}
