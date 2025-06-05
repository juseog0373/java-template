package com.nexacode.template.auth.presentation;

import com.nexacode.template.auth.application.AuthService;
import com.nexacode.template.common.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "auth", description = "인증 인가 관련 API")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "유저 ID 를 통한 회원가입 API",
            description = "ID Password 를 통해 회원가입합니다, 이메일, 아이디 중복이면 안됩니다."
    )
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @PostMapping("/auth/join")
    public BaseResponse<?> joinByIdAndPassword(@RequestBody @Valid AuthRequest.JoinByIdAndPasswordRequest request) {
        authService.joinByIdAndPassword(request);
        return BaseResponse.success();
    }

    @Operation(
            summary = "ID 중복 확인 체크 API",
            description = "중복이면 true, 사용가능한 아이디면 false"
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @PostMapping("/auth/is-exist")
    public BaseResponse<Boolean> hasLoginIdExist(@RequestBody @Valid AuthRequest.DuplicateCheckIdRequest request) {
        return BaseResponse.success(authService.hasLoginIdExist(request));
    }

    @Operation(
            summary = "ID 를 통한 로그인 API",
            description = "ID Password 를 통해 로그인합니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @PostMapping("/auth/login-by-id")
    public BaseResponse<AuthResponse.LoginResponse> loginById(@RequestBody @Valid AuthRequest.LoginByIdAndPasswordRequest request) {
        AuthResponse.LoginResponse response = authService.loginById(request);
        return BaseResponse.success(response);
    }

    @Operation(
            summary = "소셜 로그인 API",
            description = "소셜 로그인 API 입니다. 각 소셜 제공업체의 token을 받아 회원가입을 지원합니다. 소셜 제공업체 ex) APPLE: 애플, GOOGLE: 구글, NAVER: 네이버, KAKAO: 카카오"
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @PostMapping("/auth/login-by-social")
    public BaseResponse<AuthResponse.LoginResponse> loginBySocial(@RequestBody @Valid AuthRequest.LoginBySocialRequest request) {
        return BaseResponse.success(authService.loginBySocial(request));
    }

    @Operation(
            summary = "access 토큰 재발급 API",
            description = "refresh token 을 활용하여 access 토큰 재발급 API"
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @PostMapping("/auth/access-by-refresh")
    public BaseResponse<AuthResponse.AccessTokenResponse> getAccessByRefresh(@RequestBody @Valid AuthRequest.RefreshTokenRequest request) {
        return BaseResponse.success(authService.getAccessByRefresh(request));
    }
}
