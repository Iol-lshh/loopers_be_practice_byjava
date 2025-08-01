package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.UserCommand;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @PostMapping("")
    @Override
    public ApiResponse<UserV1Dto.UsersResponse> signUp(
            @RequestBody UserV1Dto.UsersSignUpRequest request
    ) {
        UserCommand.Create command = request.toCommand();
        UserInfo info = userFacade.signUp(command);
        UserV1Dto.UsersResponse response = UserV1Dto.UsersResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UsersResponse> getMyInfo(
            @RequestHeader("X-USER-ID") String loginId
    ) {
        UserInfo info = userFacade.get(loginId);
        UserV1Dto.UsersResponse response = UserV1Dto.UsersResponse.from(info);
        return ApiResponse.success(response);
    }
}
