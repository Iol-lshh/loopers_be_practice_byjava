package com.loopers.domain.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductService productService;

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

}
