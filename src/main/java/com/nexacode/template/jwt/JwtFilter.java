package com.nexacode.template.jwt;


import com.nexacode.template.config.WebSecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String token = jwtProvider.extractToken(request);
        final String loginId;
        final String userId;
        final String authorities;
        final String email;

        // RELATED_LOGIN 경로는 jwt 인증 skip
        String requestURI = request.getRequestURI();
        if (isExcludePath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // SecurityContextHolder.getContext().getAuthentication() != null
        if (token == null || !jwtProvider.validateToken(token)) {
            log.info("JWT validation failed");
            filterChain.doFilter(request, response);
            return;
        }

        loginId = jwtProvider.extractLoginId(token);
        userId = jwtProvider.extractUserIdx(token);
        authorities = jwtProvider.extractAuthorities(token);
        if (userId == null || authorities == null) {
            log.info("JWT stored data invalid");
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = jwtProvider.getAuthentication(loginId, authorities, userId);
        if (auth == null) {
            log.info("JWT user not found");
            filterChain.doFilter(request, response);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean isExcludePath(String requestURI) {
        for (String excludedPath : WebSecurityConfig.RELATED_LOGIN) {
            if (requestURI.equals(excludedPath)) {
                return true;
            }
        }

        for (String excludedPath : WebSecurityConfig.AUTH_WHITELIST) {
            if (requestURI.equals(excludedPath)) {
                return true;
            }
        }

        return false;
    }
}