package com.loopers.application.brand;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BrandUsecaseIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private BrandFacade brandFacade;
    @Autowired
    private BrandService brandService;

    @DisplayName("브랜드 조회")
    @Nested
    class Get{
        @DisplayName("존재하는 브랜드 ID로 조회 시, 해당 브랜드 정보가 반환된다.")
        @Test
        public void returnBrandInfo_whenBrandExists(){
            // given
            BrandEntity existingBrand = brandService.create("Test Brand");
            assertNotNull(existingBrand.getId());

            // when
            var brandInfo = brandFacade.get(existingBrand.getId());

            // then
            assertNotNull(brandInfo);
            assertEquals(existingBrand.getId(), brandInfo.id());
        }

        @DisplayName("존재하지 않는 브랜드 ID로 조회 시, NOT_FOUND 예외가 발생한다.")
        @Test
        public void throwNotFound_whenBrandNotExists() {
            // given
            Long nonExistentBrandId = 999L; // Assuming this ID does not exist
            Optional<BrandEntity> nonExistingBrand = brandService.find(nonExistentBrandId);
            assertTrue(nonExistingBrand.isEmpty());

            // when
            var result =  assertThrows(CoreException.class, () -> brandFacade.get(nonExistentBrandId));

            // then
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }
    }
}
