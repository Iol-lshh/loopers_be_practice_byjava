package com.loopers.interfaces.api.users;

import com.loopers.application.users.UsersFacade;
import com.loopers.application.users.UsersInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.type.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UsersV1Controller implements UsersV1ApiSpec {

    private final UsersFacade usersFacade;

    @PostMapping("")
    @Override
    public ApiResponse<UsersV1Dto.UsersResponse> register(
            @RequestBody UsersV1Dto.UsersRegisterRequest request
    ) {
        UsersInfo info = usersFacade.register(
                request.loginId(),
                Gender.from(request.gender()),
                request.birthDate(),
                request.email());
        UsersV1Dto.UsersResponse response = UsersV1Dto.UsersResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UsersV1Dto.UsersResponse> getMyInfo(
            @RequestHeader("X-USER-ID") String loginId
    ) {
        UsersInfo info = usersFacade.getMyInfo(loginId);
        UsersV1Dto.UsersResponse response = UsersV1Dto.UsersResponse.from(info);
        return ApiResponse.success(response);
    }
}
