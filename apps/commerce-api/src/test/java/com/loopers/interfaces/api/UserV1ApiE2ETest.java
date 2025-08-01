package com.loopers.interfaces.api;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMapAdapter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {

    private static final Function<String, String> ENDPOINT = (subRoute) -> "/api/v1/users" + subRoute;

    private final TestRestTemplate testRestTemplate;
    private final UserService userService;
    private final DatabaseCleanUp databaseCleanUp;


    @Autowired
    public UserV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            UserService userService,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userService = userService;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class Register {
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsCreatedUser_whenRegistrationIsSuccessful() {
            // arrange
            String requestUrl = ENDPOINT.apply("");
            UserV1Dto.UsersSignUpRequest request = new UserV1Dto.UsersSignUpRequest(
                "testuser",
                "남",
                "1990-01-01",
                "test@example.com"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                testRestTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data().id());
            assertEquals(request.loginId(), response.getBody().data().loginId());
            assertEquals(request.gender(), response.getBody().data().gender());
            assertEquals(request.birthDate(), response.getBody().data().birthDate());
            assertEquals(request.email(), response.getBody().data().email());
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenGenderIsInvalid() {
            // arrange
            String requestUrl = ENDPOINT.apply("");
            UserV1Dto.UsersSignUpRequest request = new UserV1Dto.UsersSignUpRequest(
                "testuser",
                null,
                "1990-01-01",
                "test@example.com"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                testRestTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class GetMyInfo {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsMyInfo_whenRequestIsSuccessful() {
            // arrange
            String requestUrl = ENDPOINT.apply("/me");
            var signUpCommand = new UserCommand.Create(
                    "testuser", UserEntity.Gender.MALE, "1993-04-09", "test@gmail.com");
            var testUser = userService.create(signUpCommand);

            // act
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(testUser.getLoginId())));
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                    testRestTemplate.exchange(
                            requestUrl,
                            HttpMethod.GET,
                            new HttpEntity<>(null, headers),
                            responseType
                    );

            // assert
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().data().id());
            assertEquals(testUser.getLoginId(), response.getBody().data().loginId());
            assertEquals(testUser.getGender().getValue(), response.getBody().data().gender());
            assertEquals(testUser.getBirthDate(), response.getBody().data().birthDate());
            assertEquals(testUser.getEmail(), response.getBody().data().email());
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void throwsNotFound_whenUserDoesNotExist() {
            // arrange
            String requestUrl = ENDPOINT.apply("/me");
            String nonExistentLoginId = "nonexistentuser";
            var headers = new MultiValueMapAdapter<>(Map.of("X-USER-ID", List.of(nonExistentLoginId)));

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                    testRestTemplate.exchange(
                            requestUrl,
                            HttpMethod.GET,
                            new HttpEntity<>(null, headers),
                            responseType
                    );

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

    }
}
