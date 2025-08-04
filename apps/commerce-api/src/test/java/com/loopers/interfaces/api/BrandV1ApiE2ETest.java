package com.loopers.interfaces.api;

import com.loopers.application.brand.BrandFacade;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.brand.BrandV1Dto;
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

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrandV1ApiE2ETest {

    private static final Function<String, String> ENDPOINT = (subRoute) -> "/api/v1/brands" + subRoute;

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final BrandService brandService;
    private final BrandFacade brandFacade;

    @Autowired
    public BrandV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            DatabaseCleanUp databaseCleanUp, BrandService brandService, BrandFacade brandFacade
    ) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.brandService = brandService;
        this.brandFacade = brandFacade;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/brands/{brandId}")
    @Nested
    class GetBrandById {

        @DisplayName("존재하는 브랜드 ID를 주면, 해당 브랜드 정보를 반환한다.")
        @Test
        void returnsBrandInfo_whenValidIdIsProvided() {
            // Begin
            String requestUrl = ENDPOINT.apply("");
            var brand = brandService.create("Test Brand");
            assertTrue(brandService.find(brand.getId()).isPresent());

            // when
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.Response>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl+ "/" + brand.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data());
            assertEquals(brand.getId(), response.getBody().data().id());
            assertEquals(brand.getName(), response.getBody().data().name());
        }

        @DisplayName("존재하지 않는 브랜드 ID를 주면, 404 NOT FOUND 응답을 반환한다.")
        @Test
        void returnsNotFound_whenInvalidIdIsProvided() {
            // Begin
            String requestUrl = ENDPOINT.apply("");
            long invalidBrandId = 9999L;
            assertTrue(brandService.find(invalidBrandId).isEmpty());

            // when
            var response = testRestTemplate.exchange(
                    requestUrl + "/" + invalidBrandId,
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    new ParameterizedTypeReference<ApiResponse<BrandV1Dto.Response>>() {}
            );

            // then
            assertTrue(response.getStatusCode().is4xxClientError());
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}
