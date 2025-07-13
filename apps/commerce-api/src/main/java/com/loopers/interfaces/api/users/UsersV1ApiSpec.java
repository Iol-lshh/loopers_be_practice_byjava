package com.loopers.interfaces.api.users;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users V1 API", description = "회원가입, 내 정보 조회")
public interface UsersV1ApiSpec {
    @Operation(
            summary = "회원가입",
            description = "회원가입"
    )
    ApiResponse<UsersV1Dto.UsersResponse> register(
            @Schema(name = "요청", description = "회원가입할 사용자 정보 입력")
            UsersV1Dto.UsersRegisterRequest request
    );

    @Operation(
            summary = "내 정보 조회",
            description = "내 정보 조회"
    )
    ApiResponse<UsersV1Dto.UsersResponse> getMyInfo(
            @Schema(name = "조회할 아이디", description = "X-USER-ID") String loginId
    );
}
