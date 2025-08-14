package com.loopers.application.product;

import com.loopers.application.like.LikeFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.*;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ProductUsecaseIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private LikeFacade likeFacade;
    @Autowired
    private ProductRepository productRepository;
    @MockitoSpyBean
    private ProductCacheRepository productCacheRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }
    @AfterEach
    void tearDownCache() {
        redisCleanUp.truncateAll();
    }

    @Autowired
    private BrandService brandService;
    @MockitoSpyBean
    private ProductService productService;
    @Autowired
    private ProductFacade productFacade;
    @Autowired
    private UserService userService;

    private BrandEntity prepareBrand() {
        BrandEntity brand = brandService.create("Test Brand");
        assertNotNull(brand.getId());
        return brand;
    }
    private ProductEntity prepareProduct(BrandEntity brand) {
        ProductCommand.Register beforeRegister = new ProductCommand.Register(
                "Test Product",
                brand.getId(),
                10000L,
                1L
        );
        var result = productService.register(beforeRegister);
        assertNotNull(result.getId());
        return result;
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
    private ProductEntity prepareProduct(long price){
        BrandEntity brand = prepareBrand();
        ProductCommand.Register beforeRegister = new ProductCommand.Register(
                "Test Product",
                brand.getId(),
                price,
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
    private ProductEntity prepareProduct(long price, long stock) {
        BrandEntity brand = prepareBrand();
        ProductCommand.Register beforeRegister = new ProductCommand.Register(
                "Test Product",
                brand.getId(),
                price,
                stock
        );
        var result = productService.register(beforeRegister);
        assertNotNull(result.getId());
        return result;
    }
    private UserEntity prepareUser() {
        var prepareUserCommand = UserCommand.Create.of("testUser", "남", "1993-04-05", "test@gmail.com");
        UserEntity user = userService.create(prepareUserCommand);
        assertTrue(userService.find(user.getId()).isPresent());
        return user;
    }


    @Autowired
    private ProductJpaRepository jpaRepository;

    @Nested
    @DisplayName("상품 목록 조회")
    class List {

        @DisplayName("상품 목록을 조회할 때, 상품 정보가 반환된다.")
        @Test
        void returnProductList_whenQuery() {
            // given
            BrandEntity preparedBrand = prepareBrand();
            prepareProduct(preparedBrand);
            var list = jpaRepository.findAll();
            assertFalse(list.isEmpty());

            // when
            var productList = productFacade.list(Pageable.ofSize(10));

            // then
            assertFalse(productList.isEmpty());
        }

        @DisplayName("상품 목록을 조회할 때, 상품 정보가 없으면 빈 리스트를 반환한다.")
        @Test
        void returnEmptyList_whenNoProducts() {
            // given
            var list = jpaRepository.findAll();
            assertTrue(list.isEmpty());

            // when
            var productList = productFacade.list(Pageable.ofSize(10));

            // then
            assertTrue(productList.isEmpty());
        }

        @DisplayName("상품 목록을 조회할 때, 페이지 정보가 적용된다.")
        @Test
        void returnProductListWithPagination_whenQuery() {
            // given
            BrandEntity preparedBrand = prepareBrand();
            for (int i = 0; i < 11; i++) {
                prepareProduct(preparedBrand);
            }
            var list = jpaRepository.findAll();
            assertEquals(11, list.size());

            // when
            var productList = productFacade.list(Pageable.ofSize(10));

            // then
            assertEquals(10, productList.size());
        }

        @DisplayName("상품 목록을 조회할 때, 페이지 정보가 적용되어 다음 페이지가 있다.(default. Order by latest")
        @Test
        void returnProductListWithNextPage_whenQuery() {
            // given
            BrandEntity preparedBrand = prepareBrand();
            String nextPageProductName = "Oldest Next Page Product";
            var productOldest =  prepareProduct(nextPageProductName);
            productFacade.release(productOldest.getId());
            for (int i = 0; i < 10; i++) {
                var product = prepareProduct(preparedBrand);
                productFacade.release(product.getId());
            }
            var list = jpaRepository.findAll();
            assertEquals(11, list.size());

            // when
            var productList = productFacade.list(PageRequest.of(1, 10));

            // then
            assertEquals(1, productList.size());
            assertEquals(nextPageProductName, productList.getFirst().name());
        }

        @Nested
        @DisplayName("Order by Created")
        class OrderByCreated {

            @DisplayName("최신순으로 상품 목록을 조회할 때, 가장 최신 생성순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByLatest_whenQuery() {
                // given
                ProductEntity firstProduct = prepareProduct("Product1");
                productFacade.release(firstProduct.getId());
                ProductEntity secondProduct = prepareProduct("Product2");
                productFacade.release(secondProduct.getId());

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.CreatedAt(false))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(secondProduct.getId(), productList.get(0).id());
                assertEquals(firstProduct.getId(), productList.get(1).id());
            }

            @DisplayName("최신순으로 상품 목록을 조회할 때, 가장 오래된 생성순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByOldest_whenQuery() {
                // given
                ProductEntity firstProduct = prepareProduct("Product1");
                ProductEntity secondProduct = prepareProduct("Product2");

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.CreatedAt(true))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }
        }

        @Nested
        @DisplayName("Order by Released")
        class OrderByReleased {

            @DisplayName("최신순으로 상품 목록을 조회할 때, 가장 최신 생성순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByLatest_whenQuery() {
                // given
                ProductEntity firstProduct = prepareProduct("Product1");
                firstProduct.release();
                productRepository.save(firstProduct);
                ProductEntity secondProduct = prepareProduct("Product2");
                secondProduct.release();
                productRepository.save(secondProduct);

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.ReleasedAt(false))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(secondProduct.getId(), productList.get(0).id());
                assertEquals(firstProduct.getId(), productList.get(1).id());
            }

            @DisplayName("최신순으로 상품 목록을 조회할 때, 가장 오래된 생성순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByOldest_whenQuery() {
                // given
                ProductEntity firstProduct = prepareProduct("Product1");
                ProductEntity secondProduct = prepareProduct("Product2");

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.ReleasedAt(true))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }
        }

        @Nested
        @DisplayName("Order by Price")
        class ByPrice {

            @DisplayName("가격순으로 상품 목록을 조회할 때, 가격이 낮은 순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByPriceAscending_whenQuery() {
                // given
                ProductEntity firstProduct = prepareProduct(1L);
                ProductEntity secondProduct = prepareProduct(10L);

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.Price(true))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }

            @DisplayName("가격순으로 상품 목록을 조회할 때, 가격이 높은 순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByPriceDescending_whenQuery() {
                // given
                ProductEntity firstProduct = prepareProduct(1L);
                productFacade.release(firstProduct.getId());
                ProductEntity secondProduct = prepareProduct(10L);
                productFacade.release(secondProduct.getId());

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.Price(false))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(secondProduct.getId(), productList.get(0).id());
                assertEquals(firstProduct.getId(), productList.get(1).id());
            }
        }

        @Nested
        @DisplayName("Order by Like Count")
        class ByLikeCount {

            @DisplayName("좋아요 수순으로 상품 목록을 조회할 때, 좋아요 수가 많은 순으로 상품이 반환된다.")
            @Test
            void returnProductListOrderedByLikeCountDescending_whenQuery() {
                // given
                UserEntity preparedUser = prepareUser();
                ProductEntity firstProduct = prepareProduct("Product1");
                ProductEntity secondProduct = prepareProduct("Product2");
                likeFacade.likeProduct(preparedUser.getId(), firstProduct.getId());

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .orderBy(new ProductStatement.LikeCount())
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }
        }

        @Nested
        @DisplayName("Brand ID 핕터링")
        class ByBrandID {

            @DisplayName("브랜드 ID로 상품 목록을 조회할 때, 해당 브랜드의 상품만 반환된다.")
            @Test
            void returnProductListByBrandID_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand);
                ProductEntity secondProduct = prepareProduct("Other Product");

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(1, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
            }

            @DisplayName("브랜드 ID로 상품 목록을 조회할 때, 해당 브랜드의 상품이 없으면 빈 리스트를 반환한다.")
            @Test
            void returnEmptyList_whenNoProductsForBrand() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand);
                prepareProduct("Other Product");

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(1, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
            }

            @DisplayName("브랜드 ID와 최신 순으로 상품 목록을 조회할 때, 해당 브랜드의 상품이 최신순으로 반환된다.")
            @Test
            void returnProductListByBrandIDOrderedByLatest_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand);
                productFacade.release(firstProduct.getId());
                ProductEntity secondProduct = prepareProduct(preparedBrand);
                productFacade.release(secondProduct.getId());

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .orderBy(new ProductStatement.CreatedAt(false))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(secondProduct.getId(), productList.get(0).id());
                assertEquals(firstProduct.getId(), productList.get(1).id());
            }

            @DisplayName("브랜드 ID와 오래된 순으로 상품 목록을 조회할 때, 해당 브랜드의 상품이 오래된 순으로 반환된다.")
            @Test
            void returnProductListByBrandIDOrderedByOldest_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand);
                ProductEntity secondProduct = prepareProduct(preparedBrand);

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .orderBy(new ProductStatement.CreatedAt(true))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }

            @DisplayName("브랜드 ID와 낮은 가격 순으로 상품 목록을 조회할 때, 해당 브랜드의 상품이 가격순으로 반환된다.")
            @Test
            void returnProductListByBrandIDOrderedByPrice_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand);
                ProductEntity secondProduct = prepareProduct(preparedBrand);

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .orderBy(new ProductStatement.Price(true))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }

            @DisplayName("브랜드 ID와 높은 가격 순으로 상품 목록을 조회할 때, 해당 브랜드의 상품이 가격순으로 반환된다.")
            @Test
            void returnProductListByBrandIDOrderedByPriceDescending_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand, 1L);
                productFacade.release(firstProduct.getId());
                ProductEntity secondProduct = prepareProduct(preparedBrand, 10L);
                productFacade.release(secondProduct.getId());

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .orderBy(new ProductStatement.Price(false))
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(secondProduct.getId(), productList.get(0).id());
                assertEquals(firstProduct.getId(), productList.get(1).id());
            }

            @DisplayName("브랜드 ID와 좋아요 수 순으로 상품 목록을 조회할 때, 해당 브랜드의 상품이 좋아요 수순으로 반환된다.")
            @Test
            void returnProductListByBrandIDOrderedByLikeCount_whenQuery() {
                // given
                UserEntity preparedUser = prepareUser();
                BrandEntity preparedBrand = prepareBrand();
                ProductEntity firstProduct = prepareProduct(preparedBrand);
                ProductEntity secondProduct = prepareProduct(preparedBrand);
                likeFacade.likeProduct(preparedUser.getId(), firstProduct.getId());

                // when
                ProductStatement criteria = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .orderBy(new ProductStatement.LikeCount())
                        .build();
                var productList = productFacade.list(criteria, Pageable.ofSize(10));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(2, productList.size());
                assertEquals(firstProduct.getId(), productList.get(0).id());
                assertEquals(secondProduct.getId(), productList.get(1).id());
            }
        }

        @DisplayName("상품 목록 조회 캐시")
        @Nested
        class ProductCacheTest {
            @DisplayName("상품 목록을 조회할 때, 캐시가 적용되어 빠르게 응답한다.")
            @Test
            void returnCachedProductList_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                for (int i = 0; i < 20; i++) {
                    prepareProduct(preparedBrand);
                }
                ProductStatement statement = ProductStatement.builder()
                        .orderBy(new ProductStatement.CreatedAt(false))
                        .build();
                var list = productFacade.list(statement, Pageable.ofSize(20));
                assertEquals(20, list.size());
                verify(productService, times(1)).findWithSignals(any(ProductStatement.class), any(Pageable.class));
                verify(productCacheRepository, times(1)).findIds(any(ProductStatement.class), any(Pageable.class));
                verify(productCacheRepository, times(1)).save(any(ProductStatement.class), any(Pageable.class), anyList());

                // when
                var productList = productFacade.list(statement, Pageable.ofSize(20));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(20, productList.size());
                verify(productService, times(1)).findWithSignals(anyList()); // 캐시된 ID로 조회
                verify(productCacheRepository, times(2)).findIds(any(ProductStatement.class), any(Pageable.class)); // 총 2번 호출
            }

            @DisplayName("상품 목록을 조회할 때, 캐시가 적용되어 빠르게 응답한다. (브랜드 ID 필터링)")
            @Test
            void returnCachedProductListByBrandID_whenQuery() {
                // given
                BrandEntity preparedBrand = prepareBrand();
                for (int i = 0; i < 20; i++) {
                    prepareProduct(preparedBrand);
                }
                ProductStatement statement = ProductStatement.builder()
                        .brandID(preparedBrand.getId())
                        .orderBy(new ProductStatement.CreatedAt(false))
                        .build();
                var list = productFacade.list(statement, Pageable.ofSize(20));
                assertEquals(20, list.size());
                verify(productService, times(1)).findWithSignals(any(ProductStatement.class), any(Pageable.class));
                verify(productCacheRepository, times(1)).findIds(any(ProductStatement.class), any(Pageable.class));
                verify(productCacheRepository, times(1)).save(any(ProductStatement.class), any(Pageable.class), anyList());
                // when
                var productList = productFacade.list(statement, Pageable.ofSize(20));
                // then
                assertFalse(productList.isEmpty());
                assertEquals(20, productList.size());
                verify(productService, times(1)).findWithSignals(anyList()); // 캐시된
                // ID로 조회
                verify(productCacheRepository, times(2)).findIds(any(ProductStatement.class), any(Pageable.class)); // 총 2번 호출
            }
        }
    }

    @Nested
    @DisplayName("상품 정보 조회")
    class Get {
        @DisplayName("존재하는 상품을 조회할 때, 상품 정보가 반환된다.")
        @Test
        void returnProductInfo_whenProductExists() {
            // given
            BrandEntity preparedBrand = prepareBrand();
            ProductEntity prepared = prepareProduct(preparedBrand);
            // when
            var productInfo = productFacade.get(prepared.getId());

            // then
            assertNotNull(productInfo);
            assertEquals(prepared.getId(), productInfo.id());
            assertEquals(prepared.getName(), productInfo.name());
            assertEquals(prepared.getBrandId(), productInfo.brandId());
        }

        @DisplayName("존재하지 않는 상품을 조회할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwException_whenProductNotExists() {
            // given
            Long nonExistentId = 999L;
            Optional<ProductEntity> productOptional = productService.find(nonExistentId);
            assertTrue(productOptional.isEmpty());

            // when & then
            var result = assertThrows(CoreException.class, () -> productFacade.get(nonExistentId));

            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

    }


    @Nested
    @DisplayName("상품 출시")
    class Release {
        @DisplayName("존재하는 상품을 출시할 때, 상품 정보가 반환된다.")
        @Test
        void returnProductInfo_whenReleaseProduct() {
            // given
            BrandEntity preparedBrand = prepareBrand();
            ProductEntity prepared = prepareProduct(preparedBrand);
            assertEquals(ProductEntity.State.StateType.CLOSED, prepared.getState().getValue());

            // when
            var productInfo = productFacade.release(prepared.getId());

            // then
            assertNotNull(productInfo);
            assertEquals(ProductEntity.State.StateType.OPEN.name(), productInfo.state());
            assertEquals(prepared.getId(), productInfo.id());
        }

        @DisplayName("존재하지 않는 상품을 출시할 때, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwException_whenReleaseNonExistentProduct() {
            // given
            Long nonExistentId = 999L;
            Optional<ProductEntity> productOptional = productService.find(nonExistentId);
            assertTrue(productOptional.isEmpty());

            // when & then
            var result = assertThrows(CoreException.class, () -> productFacade.release(nonExistentId));

            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }
    }

    @Nested
    @DisplayName("상품 재고 차감")
    class Deduct {
        @DisplayName("존재하는 상품의 재고를 차감할 때, 재고가 차감된 상품 정보가 반환된다.")
        @Test
        void returnDeductedProduct_whenDeductStock() {
            // given
            long stock = 10L;
            ProductEntity prepared = prepareProduct(10L, stock);
            productService.release(prepared.getId());
            assertEquals(stock, prepared.getStock());

            // when
            Map<Long, Long> orderQuantityList = Map.of(prepared.getId(), 1L);
            var deductedProducts = productService.deduct(orderQuantityList);

            // then
            assertNotNull(deductedProducts);
            assertEquals(1, deductedProducts.size());
            ProductEntity updatedProduct = deductedProducts.get(0);
            assertEquals(prepared.getId(), updatedProduct.getId());
            assertEquals(stock - 1, updatedProduct.getStock());
        }

        @DisplayName("동시에 존재하는 상품의 재고를 차감할 때, 순차적으로 차감된다.")
        @Test
        void returnDeductedProduct_whenDeductStockByMultipleUsers() throws InterruptedException {
            // given
            long stock = 9L;
            ProductEntity prepared = prepareProduct(10L, stock);
            productService.release(prepared.getId());
            assertEquals(stock, prepared.getStock());

            // when
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        Map<Long, Long> orderQuantityList = Map.of(prepared.getId(), 1L);
                        productService.deduct(orderQuantityList);
                    } catch (CoreException e) {
                        assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            // then
            ProductEntity updatedProduct = productService.find(prepared.getId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
            assertEquals(0L, updatedProduct.getStock());
        }

    }
}

