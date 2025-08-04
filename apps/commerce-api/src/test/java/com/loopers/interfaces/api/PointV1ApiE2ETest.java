package com.loopers.interfaces.api;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.point.PointV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMapAdapter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

    private static final Function<String, String> ENDPOINT = (subRoute) -> "/api/v1/points" + subRoute;

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private final UserService userService;

    @Autowired
    public PointV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            DatabaseCleanUp databaseCleanUp,
            UserService userService
    ) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.userService = userService;
    }

    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class Charge {
        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsChargedPoints_whenUserChargesPoints() {
            // arrange
            String requestUrl = ENDPOINT.apply("/charge");
            var signUpCommand = new UserCommand.Create("tester", UserEntity.Gender.MALE, "1993-04-09", "test@gmail.com");
            var testUser = userService.create(signUpCommand);
            assertTrue(userService.find(testUser.getId()).isPresent());
            long pointsToCharge = 1000;
            var request = new PointV1Dto.PointsChargeRequest(testUser.getId(), pointsToCharge);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    responseType
            );

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(testUser.getId(), response.getBody().data().userId());
            assertEquals(request.amount(), response.getBody().data().amount());
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFound_whenUserDoesNotExist() {
            // arrange
            String requestUrl = ENDPOINT.apply("/charge");
            Long nonExistentUserId = 999L; // Assuming this user ID does not exist
            assertTrue(userService.find(nonExistentUserId).isEmpty());
            long pointsToCharge = 1000;
            var request = new PointV1Dto.PointsChargeRequest(nonExistentUserId, pointsToCharge);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    responseType
            );

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @DisplayName("GET /api/v1/points")
    @Nested
    class Get {
        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsPoints_whenUserRequestsPoints() {
            // arrange
            String requestUrl = ENDPOINT.apply("");
            var signUpCommand = new UserCommand.Create("tester", UserEntity.Gender.MALE, "1993-04-09", "test@gmail.com");
            var testUser = userService.create(signUpCommand);
            assertTrue(userService.find(testUser.getId()).isPresent());

            // act
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(testUser.getId().toString())));
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    responseType
            );

            // assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testUser.getId(), response.getBody().data().userId());
            assertTrue(response.getBody().data().amount() >= 0);
        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Order 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            String requestUrl = ENDPOINT.apply("");

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointsResponse>> responseType = new ParameterizedTypeReference<>() {};
            var response = testRestTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    responseType
            );

            // assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }
}
