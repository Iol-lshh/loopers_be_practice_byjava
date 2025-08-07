package com.loopers.interfaces.api;

import com.loopers.application.like.LikeFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.like.LikeV1Dto;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikeV1ApiE2ETest {

    private static final String ENDPOINT = "/api/v1/like/products";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    private final BrandService brandService;
    private final ProductService productService;
    private final UserService userService;
    private final LikeFacade likeFacade;

    @Autowired
    public LikeV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp, BrandService brandService, ProductService productService, UserService userService, LikeFacade likeFacade) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.brandService = brandService;
        this.productService = productService;
        this.userService = userService;
        this.likeFacade = likeFacade;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private BrandEntity prepareBrand() {
        BrandEntity brand = brandService.create("Test Brand");
        assertNotNull(brand.getId());
        return brand;
    }
    private ProductEntity prepareProduct(String productName) {
        BrandEntity brand = prepareBrand();
        ProductCommand.Register beforeRegister = new ProductCommand.Register(
                productName,
                brand.getId(),
                10000L,
                1L
        );
        var result = productService.register(beforeRegister);
        assertNotNull(result.getId());
        return result;
    }
    private ProductEntity prepareProduct(BrandEntity preparedBrand, long price) {
        ProductCommand.Register beforeRegister = new ProductCommand.Register(
                "Test Product",
                preparedBrand.getId(),
                price,
                1L
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

    @DisplayName("POST /api/v1/like/products/{productId}")
    @Nested
    class LikeProduct {

        @DisplayName("존재하는 상품 ID를 주면, 해당 상품에 대한 좋아요를 추가한다.")
        @Test
        void addsLikeToProduct_whenValidProductIdIsProvided() {
            // given
            ProductEntity product = prepareProduct("Test Product");
            UserEntity user = prepareUser("testUser");

            // when
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(user.getId().toString())));
            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    ENDPOINT + "/" + product.getId(),
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    responseType
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(product.getId(), response.getBody().data().targetId());
            assertEquals(user.getId(), response.getBody().data().userId());
            assertEquals("PRODUCT", response.getBody().data().targetType());
            assertTrue(response.getBody().data().isLike());
        }
    }

    @DisplayName("DELETE /api/v1/like/products/{productId}")
    @Nested
    class DislikeProduct {

        @DisplayName("존재하는 상품 ID를 주면, 해당 상품에 대한 좋아요를 제거한다.")
        @Test
        void removesLikeFromProduct_whenValidProductIdIsProvided() {
            // given
            ProductEntity product = prepareProduct("Test Product");
            UserEntity user = prepareUser("testUser");
            likeFacade.likeProduct(user.getId(), product.getId());

            // when
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(user.getId().toString())));
            ParameterizedTypeReference<ApiResponse<LikeV1Dto.Response>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    ENDPOINT + "/" + product.getId(),
                    HttpMethod.DELETE,
                    new HttpEntity<>(null, headers),
                    responseType,
                    product.getId()
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(product.getId(), response.getBody().data().targetId());
            assertEquals(user.getId(), response.getBody().data().userId());
            assertEquals("PRODUCT", response.getBody().data().targetType());
            assertFalse(response.getBody().data().isLike());
        }
    }
    @DisplayName("GET /api/v1/like/products")
    @Nested
    class GetLikedProducts {

        @DisplayName("좋아요한 상품 목록을 반환한다.")
        @Test
        void returnsLikedProducts_whenUserHasLikedProducts() {
            // given
            var requestUrl = "/api/v1/like/products";
            UserEntity user = prepareUser("testUser");
            BrandEntity brand = prepareBrand();
            ProductEntity product1 = prepareProduct(brand, 10000L);
            ProductEntity product2 = prepareProduct(brand, 20000L);
            likeFacade.likeProduct(user.getId(), product1.getId());
            likeFacade.likeProduct(user.getId(), product2.getId());

            // when
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(user.getId().toString())));
            ParameterizedTypeReference<ApiResponse<List<LikeV1Dto.Response>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    responseType
            );

            // then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(2, response.getBody().data().size());
        }
    }
}
