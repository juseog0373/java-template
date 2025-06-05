package com.nexacode.template.auth.presentation;

import com.nexacode.template.infrastructure.domain.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

public class AuthRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "JoinByIdAndPasswordRequest")
    public static class JoinByIdAndPasswordRequest {
        @NotBlank(message = "아이디를 입력해주세요")
        @Schema(description = "사용자 아이디", example = "test")
        @Length(min = 8, max = 200)
        private String loginId;

        @Schema(description = "사용자 비밀번호", example = "test1234!")
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Length(min = 8, max = 200)
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "DuplicateCheckIdRequest")
    public static class DuplicateCheckIdRequest {
        @Schema(description = "사용자 아이디", example = "test")
        @NotBlank(message = "아이디를 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영어와 숫자만 입력 가능합니다.")
        @Length(min = 8, max = 200)
        private String loginId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "LoginByIdAndPasswordRequest")
    public static class LoginByIdAndPasswordRequest {
        @Schema(description = "사용자 아이디", example = "test")
        @NotNull(message = "아이디는 필수 값입니다")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영어와 숫자만 입력 가능합니다.")
        private String loginId;

        @Schema(description = "사용자 비밀번호", example = "test1234!")
        @NotNull(message = "비밀번호는 필수 값입니다")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+=-]+$", message = "비밀번호는 영어, 숫자, 특수문자(!@#$%^&*()_+=-)만 입력 가능합니다.")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "LoginBySocialRequest")
    public static class LoginBySocialRequest{
        @NotNull(message = "token 은 필수 값입니다")
        @Schema(description = "소셜 로그인 후 업체에서 제공하는 access token")
        private String token;

        @NotNull(message = "provider 는 필수 값입니다")
        @Schema(
                description = "소셜 로그인 제공자 ex) APPLE: 애플, GOOGLE: 구글, NAVER: 네이버, KAKAO: 카카오",
                allowableValues = {"APPLE", "NAVER", "KAKAO", "GOOGLE"}
        )
        private Provider provider;

        @Schema(description = "로그인한 유저의 OS ex) IOS, ANDROID",
                allowableValues = {"IOS", "ANDROID"}
        )
        private String os;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "RefreshTokenRequest")
    public static class RefreshTokenRequest {
        @NotNull(message = "refresh token 은 필수 값입니다")
        @Schema(description = "refresh token")
        private String token;
    }
}
