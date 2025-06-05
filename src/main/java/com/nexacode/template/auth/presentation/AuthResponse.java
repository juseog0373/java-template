package com.nexacode.template.auth.presentation;

import com.nexacode.template.infrastructure.domain.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class AuthResponse {

    @Getter
    @Setter
    @Builder
    public static class LoginResponse {
        private long id;
//        private String authority;
//        private String loginId;
//        private String email;
        private String accessToken;
        private String accessExpireAt;
        private String refreshToken;
        private String refreshExpireAt;
    }

    @Getter
    @Setter
    @Builder
    public static class SocialResponse {
        private Provider provider;  // 소셜 로그인 제공자 (GOOGLE, KAKAO, NAVER, APPLE)
        private String name;      // 사용자 이름
        private String email;     // 사용자 이메일
        private String phone;     // 사용자 전화번호
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class AccessTokenResponse {
        private String accessToken;
        private String accessExpireAt;
    }
}
