package com.loopers.domain.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private ProductFacade productFacade;
    @MockitoSpyBean
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private RedisCleanUp redisCleanUp;
    @AfterEach
    void tearDownRedis() {
        redisCleanUp.truncateAll();
    }

    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductService productService;
    @MockitoSpyBean
    private ProductCacheRepository productCacheRepository;
    @MockitoSpyBean
    private ProductReader productReader;

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
        productFacade.release(result.getId());
        assertNotNull(result.getId());
        return result;
    }


    @Nested
    @DisplayName("상품 생성")
    class Create {
        @DisplayName("상품을 생성할 때, 상품 정보가 반환된다.")
        @Test
        void returnProduct_whenCreate() {
            // given
            BrandEntity brand = brandService.create("test brand");
            ProductCommand.Register command = new ProductCommand.Register(
                    "Test Product",
                    brand.getId(),
                    10000L,
                    1L
            );

            // when
            ProductEntity product = productService.register(command);

            // then
            assertNotNull(product);
            assertNotNull(product.getId());
            assertEquals(command.name(), product.getName());
            assertEquals(brand.getId(), product.getBrandId());
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
                var list = productService.findWithSignals(statement, Pageable.ofSize(20));
                assertEquals(20, list.size());
                verify(productCacheRepository, times(1)).findWithSignal(any(ProductStatement.class), any(Pageable.class));
                verify(productReader, times(1)).findWithSignals(any(ProductStatement.class), any(Pageable.class));
                verify(productCacheRepository, times(1)).save(any(ProductStatement.class), any(Pageable.class), anyList());

                // when
                var productList = productService.findWithSignals(statement, Pageable.ofSize(20));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(20, productList.size());
                verify(productCacheRepository, times(2)).findWithSignal(any(ProductStatement.class), any(Pageable.class)); // 총 2번 호출
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
                        .brandId(preparedBrand.getId())
                        .orderBy(new ProductStatement.CreatedAt(false))
                        .build();
                var list = productService.findWithSignals(statement, Pageable.ofSize(20));
                assertEquals(20, list.size());
                verify(productCacheRepository, times(1)).findWithSignal(any(ProductStatement.class), any(Pageable.class));
                verify(productReader, times(1)).findWithSignals(any(ProductStatement.class), any(Pageable.class));
                verify(productCacheRepository, times(1)).save(any(ProductStatement.class), any(Pageable.class), anyList());

                // when
                var productList = productService.findWithSignals(statement, Pageable.ofSize(20));

                // then
                assertFalse(productList.isEmpty());
                assertEquals(20, productList.size());
                verify(productCacheRepository, times(2)).findWithSignal(any(ProductStatement.class), any(Pageable.class)); // 총 2번 호출
            }
        }

}
