package com.nexacode.template.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexacode.template.auth.presentation.AuthResponse;
import com.nexacode.template.infrastructure.domain.Provider;
import com.nexacode.template.infrastructure.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocialClient {
    private static final Logger logger = LoggerFactory.getLogger(SocialClient.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${naver.oauth2.userinfo.url}")
    private String naverUserInfoUrl;

    @Value("${kakao.oauth2.userinfo.url}")
    private String kakaoUserInfoUrl;

    @Value("${apple.team.id}")
    private String appleTeamId;

    @Value("${apple.client.id}")
    private String appleClientId;

    @Value("${apple.client.google.id}")
    private String appleClientGoogleId;

    @Value("${apple.key.id}")
    private String appleKeyId;

    @Value("${apple.private.key}")
    private String applePrivateKey;

    public AuthResponse.SocialResponse getUserByNaverToken(String token) {
        try {
            HttpEntity<Void> request = createHeaderWithToken(token);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    naverUserInfoUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            String response = responseEntity.getBody();
            JsonNode data = objectMapper.readTree(response).get("response");
            String email = data.get("email").asText();
            String name = data.get("name").asText();
            String phone = data.get("phone").asText();

            return AuthResponse.SocialResponse.builder()
                    .provider(Provider.NAVER)
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .build();

        } catch (Exception e) {
            logger.error("Naver login failed", e);
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "USER_SOCIAL_LOGIN_FAIL");
        }
    }

    public AuthResponse.SocialResponse getUserByKakaoToken(String token) {
        try {
            HttpEntity<Void> request = createHeaderWithToken(token);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    kakaoUserInfoUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            String response = responseEntity.getBody();
            JsonNode data = objectMapper.readTree(response);
            JsonNode kakaoAccount = data.get("kakao_account");

            if (!kakaoAccount.has("email")) {
                throw new IllegalArgumentException("Kakao email not exist");
            }

            String phone = kakaoAccount.has("phone_number")
                    ? "0" + kakaoAccount.get("phone_number").asText().split(" ")[1].replace("-", "")
                    : "";
            String name = kakaoAccount.has("name")
                    ? kakaoAccount.get("name").asText()
                    : kakaoAccount.get("profile").get("nickname").asText();
            String email = kakaoAccount.get("email").asText();

            return AuthResponse.SocialResponse.builder()
                    .provider(Provider.KAKAO)
                    .email(email)
                    .name(name)
                    .phone(phone)
                    .build();

        } catch (Exception e) {
            logger.error("Kakao login failed", e);
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "USER_SOCIAL_LOGIN_FAIL");
        }
    }

    public AuthResponse.SocialResponse
    getUserByAppleToken(String code, String os) {
        try {
            Map<String, Object> tokenResponse = getAppleTokenResponse(code, os);
            String idToken = (String) tokenResponse.get("id_token");
            return validateAppleToken(idToken);
        } catch (Exception e) {
            log.error("Error in Apple authentication", e);
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "USER_SOCIAL_LOGIN_FAIL");
        }
    }

    private Map<String, Object> getAppleTokenResponse(String code, String os) {
        try {
            String clientId = "Android".equals(os) ? appleClientGoogleId : appleClientId;
            String clientSecret = generateClientSecret();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            String response = restTemplate.postForObject(
                    "https://appleid.apple.com/auth/token",
                    request,
                    String.class
            );
            log.debug("Apple token response: {}", response);
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error getting Apple token", e);
            throw new RuntimeException("Failed to get Apple token", e);
        }
    }

    public AuthResponse.SocialResponse validateAppleToken(String idToken) {
        try {
            String publicKeysUrl = "https://appleid.apple.com/auth/keys";
            String publicKeysResponse = restTemplate.getForObject(publicKeysUrl, String.class);
            Map publicKeys = objectMapper.readValue(publicKeysResponse, Map.class);

            // JWT 디코딩 (헤더만)
            String[] jwtParts = idToken.split("\\.");
            String headerJson = decodeJwtPart(jwtParts[0]);
            Map header = objectMapper.readValue(headerJson, Map.class);

            // 키 찾기
            List<Map<String, Object>> keys = (List<Map<String, Object>>) publicKeys.get("keys");

            Map<String, Object> matchingKey = keys.stream()
                    .filter(key -> key.get("kid").equals(header.get("kid")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid Apple public key"));

            // JWT 페이로드 디코딩
            String payloadJson = new String(Base64.getDecoder().decode(jwtParts[1]));
            Map payload = objectMapper.readValue(payloadJson, Map.class);
            String providerId = (String) payload.get("sub"); // 애플 고유 ID
            String email = (String) payload.get("email");

            if (email.isEmpty() || email.isBlank()) {
                throw new IllegalArgumentException("애플 로그인 중 회원 정보를 가져오지 못했습니다 다시 시도해주세요");
            }

            return AuthResponse.SocialResponse.builder()
                    .provider(Provider.APPLE)
                    .email(email)
                    .name("APPLE_USER")
                    .phone("")
                    .build();
        } catch (Exception e) {
            log.error("Error validating Apple token", e);
            throw new RuntimeException("Failed to validate Apple token", e);
        }
    }

    private String decodeJwtPart(String encodedPart) {
        // url-safe Base64 디코딩
        String normalizedInput = encodedPart
                .replace('-', '+')
                .replace('_', '/');

        // 4의 배수가 되도록 패딩 추가
        while (normalizedInput.length() % 4 != 0) {
            normalizedInput += '=';
        }

        byte[] decodedBytes = Base64.getDecoder().decode(normalizedInput);
        return new String(decodedBytes);
    }

    private String generateClientSecret() throws Exception {
        Instant now = Instant.now();

        // private key 디코딩 및 변환
        String privateKeyContent = applePrivateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return Jwts.builder()
                .setHeaderParam("kid", appleKeyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(appleTeamId)
                .setAudience("https://appleid.apple.com")
                .setSubject(appleClientId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(300))) // 5분
                .signWith(SignatureAlgorithm.ES256, privateKey)
                .compact();
    }

    private HttpEntity<Void> createHeaderWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
