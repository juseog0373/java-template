package com.nexacode.template.auth.application;

import com.nexacode.template.auth.presentation.AuthRequest;
import com.nexacode.template.auth.presentation.AuthResponse;
import com.nexacode.template.infrastructure.client.SocialClient;
import com.nexacode.template.infrastructure.entity.User;
import com.nexacode.template.infrastructure.entity.exception.UserNotfoundException;
import com.nexacode.template.infrastructure.repository.UserRepository;
import com.nexacode.template.jwt.JwtProvider;
import com.nexacode.template.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final SocialClient socialClient;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.token.expire-length}")
    private long accessTokenValidityInSeconds;
    @Value("${security.jwt.token.refresh-expire-length}")
    private long refreshTokenValidityInSeconds;

    @Transactional
    public void joinByIdAndPassword(AuthRequest.JoinByIdAndPasswordRequest request) {
            String encodedPwd = passwordEncoder.encode(request.getPassword());
            User user = User.builder()
                    .loginId(request.getLoginId())
                    .password(encodedPwd)
                    .build();
            userRepository.save(user);
    }

    public Boolean hasLoginIdExist(AuthRequest.DuplicateCheckIdRequest request) {
        return userRepository.existsByLoginId(request.getLoginId());
    }

    public AuthResponse.LoginResponse loginById(AuthRequest.LoginByIdAndPasswordRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId()).orElseThrow(UserNotfoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("비밀번호가 다릅니다.");

        return createLoginResponse(user);
    }

    public AuthResponse.LoginResponse loginBySocial(AuthRequest.LoginBySocialRequest request) {
        AuthResponse.SocialResponse socialResponse;
        String loginId = RandomStringUtils.randomAlphanumeric(10) + "_" + request.getProvider().name();

        switch (request.getProvider()) {
            case APPLE:
                socialResponse = socialClient.getUserByAppleToken(request.getToken(), request.getOs());
                break;
            case KAKAO:
                socialResponse = socialClient.getUserByKakaoToken(request.getToken());
                break;
            case NAVER:
                socialResponse = socialClient.getUserByNaverToken(request.getToken());
                break;
            default:
                throw new IllegalArgumentException("Unsupported social login provider: " + request.getProvider());
        }

        return userRepository.findByEmail(socialResponse.getEmail())
                .map(this::createLoginResponse)
                .orElseGet(() -> {

                    User newSocialUser = User.builder()
                            .loginId(loginId)
                            .password("")
                            .name(socialResponse.getName())
                            .email(socialResponse.getEmail())
                            .provider(socialResponse.getProvider())
                            .build();

                    userRepository.save(newSocialUser);
                    return createLoginResponse(newSocialUser);
                });
    }

    public AuthResponse.AccessTokenResponse getAccessByRefresh(AuthRequest.RefreshTokenRequest request) {
        String refreshToken = request.getToken();

        // 리프레시 토큰 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 사용자 정보 추출
        String loginId = jwtProvider.extractLoginId(refreshToken);
        String authorities = jwtProvider.extractAuthorities(refreshToken);

        if (loginId == null || authorities == null) {
            throw new IllegalArgumentException("리프레시 토큰에서 사용자 정보를 추출할 수 없습니다.");
        }

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 없습니다."));
        String newAccessToken = jwtProvider.generateAccessToken(user.getLoginId(), user.getId(), user.getEmail());
        LocalDateTime accessExpireAt = LocalDateTime.now().plusSeconds(accessTokenValidityInSeconds);

        return AuthResponse.AccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .accessExpireAt(TimeUtils.getDateTimeString(accessExpireAt))
                .build();
    }

    public AuthResponse.LoginResponse createLoginResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getLoginId(), user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getLoginId(), user.getId(), user.getEmail());

        String accessExpireAt = TimeUtils.getDateTimeString(LocalDateTime.now().plusSeconds(accessTokenValidityInSeconds));
        String refreshExpireAt = TimeUtils.getDateTimeString(LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds));

        return AuthResponse.LoginResponse.builder()
                .id(user.getId())
//                .authority(user.getRole().toString())
//                .name(user.getName())
//                .loginId(user.getName())
//                .email(user.getEmail())
                .accessToken(accessToken)
                .accessExpireAt(accessExpireAt)
                .refreshToken(refreshToken)
                .refreshExpireAt(refreshExpireAt)
                .build();
    }
}
