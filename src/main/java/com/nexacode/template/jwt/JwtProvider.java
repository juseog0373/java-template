package com.nexacode.template.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length}")
    private long accessTokenValidityInSeconds;

    @Value("${security.jwt.token.refresh-expire-length}")
    private long refreshTokenValidityInSeconds;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String loginId, Long id, String email) {
        Map<String, Object> claims = new HashMap<>();
//        String authority = "ROLE_" + adminRole.name();
//        claims.put("authorities", authority);
        claims.put("loginId", loginId);
        claims.put("email", email);

        return createToken(claims, loginId, id, accessTokenValidityInSeconds);
    }

    public String generateRefreshToken(String loginId, Long id, String email) {
        Map<String, Object> claims = new HashMap<>();
//        String authority = "ROLE_" + adminRole.name();
//        claims.put("authorities", authority);
        claims.put("loginId", loginId);
        claims.put("email", email);

        return createToken(claims, loginId, id, refreshTokenValidityInSeconds);
    }

    private String createToken(Map<String, Object> claims, String loginId, Long id, long validityInSeconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + (validityInSeconds * 1000));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(loginId)
                .setId(id.toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Nullable
    public String extractToken(HttpServletRequest request) {
        final String AUTHORIZATION = HttpHeaders.AUTHORIZATION;
        final String BEARER_TYPE = "Bearer";
        final String ACCESS_TOKEN_TYPE = "MY_TOKEN_TYPE";

        Enumeration<String> headers = request.getHeaders(AUTHORIZATION);
        while (headers.hasMoreElements()) {
            String headerString = headers.nextElement();
            if (headerString.toLowerCase().startsWith(BEARER_TYPE.toLowerCase())) {
                String tokenString = headerString.substring(BEARER_TYPE.length()).trim();
                request.setAttribute(ACCESS_TOKEN_TYPE, headerString.substring(0, BEARER_TYPE.length()).trim());
                return tokenString;
            }
        }
        return null;
    }

    @Nullable
    public Authentication getAuthentication(String loginId, String authorities, String id) {
        UserDetails user = new User(
                loginId,
                id,
                Arrays.stream(authorities.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );

        return new UsernamePasswordAuthenticationToken(user, id, user.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Nullable
    public String extractLoginId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Nullable
    public String extractUserIdx(String token) {
        return extractClaim(token, Claims::getId);
    }

    @Nullable
    public String extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("authorities", String.class);
    }

    @Nullable
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }
}
