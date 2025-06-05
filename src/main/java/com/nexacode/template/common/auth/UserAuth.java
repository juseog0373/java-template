package com.nexacode.template.common.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
@Slf4j
public class UserAuth {
    private final long id;
    private final User user;
    private final String loginId;
    private String authority;

    private UserAuth(Authentication authentication) {
        String credentials = (String) authentication.getCredentials();
        if (credentials == null || credentials.isBlank()) {
            throw new IllegalArgumentException("credentials 값이 비어있습니다.");
        }

        this.id = Long.parseLong(credentials);
        this.user = (User) authentication.getPrincipal();
        this.loginId = (String) authentication.getDetails();

        Optional<? extends GrantedAuthority> first = authentication.getAuthorities().stream().findFirst();
        this.authority = first.map(GrantedAuthority::getAuthority).orElse("ROLE_EMPTY");
    }

    public static UserAuth formSecurityContextHolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", authentication);
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return new UserAuth(authentication);
    }
}
