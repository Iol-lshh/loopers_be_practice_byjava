package com.loopers.interfaces.api;

import com.loopers.application.like.LikeFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.product.ProductV1Dto;
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
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private final BrandService brandService;
    private final ProductService productService;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final LikeFacade likeFacade;

    private static final Function<String, String> ENDPOINT = (subRoute) -> "/api/v1/products" + subRoute;

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

    @Autowired
    public ProductV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp, BrandService brandService, ProductService productService, UserService userService, ProductRepository productRepository, LikeFacade likeFacade) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.brandService = brandService;
        this.productService = productService;
        this.userService = userService;
        this.productRepository = productRepository;
        this.likeFacade = likeFacade;
    }

    @DisplayName("GET /api/v1/products/{productId}")
    @Nested
    class GetProductById {

        @DisplayName("존재하는 상품 ID를 주면, 해당 상품 정보를 반환한다.")
        @Test
        void returnsProductInfo_whenValidIdIsProvided() {
            // Given
            String endpoint = ENDPOINT.apply("");
            ProductEntity productEntity = prepareProduct("Test Product");

            // When
            ParameterizedTypeReference<ApiResponse<ProductV1Dto.DetailResponse>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "/" + productEntity.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(productEntity.getId(), response.getBody().data().id());
        }

        @DisplayName("존재하지 않는 상품 ID를 주면, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFound_whenInvalidIdIsProvided() {
            // Given
            String endpoint = ENDPOINT.apply("");
            long invalidProductId = 9999L; // Assuming this product ID does not exist

            // When
            ParameterizedTypeReference<ApiResponse<ProductV1Dto.DetailResponse>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "/" + invalidProductId,
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @DisplayName("GET /api/v1/products")
    @Nested
    class GetProducts {

        @DisplayName("상품 목록을 요청하면, 모든 상품 정보를 반환한다. 이때, 기본 페이지네이션은 page 0, size 20이다.")
        @Test
        void returnsAllProducts_whenRequested() {
            // Given
            String endpoint = ENDPOINT.apply("");
            BrandEntity brand = prepareBrand();
            ProductEntity product1 = prepareProduct(brand, 10000L);
            product1.release();
            productRepository.save(product1);
            ProductEntity product2 = prepareProduct(brand, 20000L);
            product2.release();
            productRepository.save(product2);
            ProductEntity product3 = prepareProduct(brand, 30000L);
            product3.release();
            productRepository.save(product3);

            // When
            ParameterizedTypeReference<ApiResponse<List<ProductV1Dto.SummaryResponse>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(3, response.getBody().data().size());
            assertEquals(product3.getId(), response.getBody().data().get(0).id());
            assertEquals(product2.getId(), response.getBody().data().get(1).id());
            assertEquals(product1.getId(), response.getBody().data().get(2).id());
        }

        @DisplayName("상품 목록을 요청할 때, 페이지네이션이 적용되어야 한다.")
        @Test
        void returnsPaginatedProducts_whenPaginationIsApplied() {
            // Given
            String endpoint = ENDPOINT.apply("");
            BrandEntity brand = prepareBrand();
            ProductEntity product1 = prepareProduct(brand, 10000L);
            product1.release();
            productRepository.save(product1);
            ProductEntity product2 = prepareProduct(brand, 20000L);
            product2.release();
            productRepository.save(product2);
            ProductEntity product3 = prepareProduct(brand, 30000L);
            product3.release();
            productRepository.save(product3);

            // When
            ParameterizedTypeReference<ApiResponse<List<ProductV1Dto.SummaryResponse>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "?page=0&size=2",
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(2, response.getBody().data().size());

        }

        @DisplayName("브랜드 필터링으로 상품 목록을 요청할 때, 필터링이 적용되어야 한다.")
        @Test
        void returnsFilteredProducts_whenBrandFilterIsApplied() {
            // Given
            String endpoint = ENDPOINT.apply("");
            BrandEntity brand1 = prepareBrand();
            ProductEntity product1 = prepareProduct(brand1, 10000L);
            product1.release();
            productRepository.save(product1);
            BrandEntity brand2 = prepareBrand();
            ProductEntity product2 = prepareProduct(brand2, 20000L);
            product2.release();
            productRepository.save(product2);
            ProductEntity product3 = prepareProduct(brand1, 30000L);
            product3.release();
            productRepository.save(product3);

            // When
            ParameterizedTypeReference<ApiResponse<List<ProductV1Dto.SummaryResponse>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "?brandId=" + brand1.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );
            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(2, response.getBody().data().size());
            assertEquals(product3.getId(), response.getBody().data().get(0).id());
            assertEquals(product1.getId(), response.getBody().data().get(1).id());
        }

        @DisplayName("최신 상품 목록을 요청할 때, 최신 상품이 먼저 반환되어야 한다.")
        @Test
        void returnsLatestProducts_whenLatestFilterIsApplied() {
            // Given
            String endpoint = ENDPOINT.apply("");
            BrandEntity brand = prepareBrand();
            ProductEntity product1 = prepareProduct(brand, 10000L);
            product1.release();
            productRepository.save(product1);
            ProductEntity product2 = prepareProduct(brand, 20000L);
            product2.release();
            productRepository.save(product2);
            ProductEntity product3 = prepareProduct(brand, 30000L);
            product3.release();
            productRepository.save(product3);

            // When
            ParameterizedTypeReference<ApiResponse<List<ProductV1Dto.SummaryResponse>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "?sort=latest",
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(3, response.getBody().data().size());
            assertEquals(product3.getId(), response.getBody().data().get(0).id());
            assertEquals(product2.getId(), response.getBody().data().get(1).id());
            assertEquals(product1.getId(), response.getBody().data().get(2).id());
        }

        @DisplayName("가격 낮은 순으로 상품 목록을 요청할 때, 가격이 낮은 상품이 먼저 반환되어야 한다.")
        @Test
        void returnsProductsSortedByPriceAsc_whenPriceAscFilterIsApplied() {
            // Given
            String endpoint = ENDPOINT.apply("");
            BrandEntity brand = prepareBrand();
            ProductEntity product1 = prepareProduct(brand, 100L);
            product1.release();
            productRepository.save(product1);
            ProductEntity product2 = prepareProduct(brand, 10000L);
            product2.release();
            productRepository.save(product2);
            ProductEntity product3 = prepareProduct(brand, 20000L);
            product3.release();
            productRepository.save(product3);

            // When
            ParameterizedTypeReference<ApiResponse<List<ProductV1Dto.SummaryResponse>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "?sort=price_asc",
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(3, response.getBody().data().size());
            assertEquals(product1.getId(), response.getBody().data().get(0).id());
            assertEquals(product2.getId(), response.getBody().data().get(1).id());
            assertEquals(product3.getId(), response.getBody().data().get(2).id());
        }

        @DisplayName("좋아요 많은 순으로 상품 목록을 요청할 때, 좋아요가 많은 상품이 먼저 반환되어야 한다.")
        @Test
        void returnsProductsSortedByLikesDesc_whenLikesDescFilterIsApplied() {
            // Given
            UserEntity user = prepareUser("test1");
            UserEntity user2 = prepareUser("test2");
            String endpoint = ENDPOINT.apply("");
            BrandEntity brand = prepareBrand();
            ProductEntity product1 = prepareProduct(brand, 10000L);
            product1.release();
            productRepository.save(product1);
            likeFacade.likeProduct(user.getId(), product1.getId());
            likeFacade.likeProduct(user2.getId(), product1.getId());
            ProductEntity product2 = prepareProduct(brand, 20000L);
            product2.release();
            productRepository.save(product2);
            likeFacade.likeProduct(user.getId(), product2.getId());
            ProductEntity product3 = prepareProduct(brand, 30000L);
            product3.release();
            productRepository.save(product3);

            // When
            ParameterizedTypeReference<ApiResponse<List<ProductV1Dto.SummaryResponse>>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    endpoint + "?sort=likes_desc",
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(3, response.getBody().data().size());
            assertEquals(product1.getId(), response.getBody().data().get(0).id());
            assertEquals(product2.getId(), response.getBody().data().get(1).id());
            assertEquals(product3.getId(), response.getBody().data().get(2).id());
        }
    }

}
