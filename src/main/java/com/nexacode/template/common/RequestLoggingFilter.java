package com.nexacode.template.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String[] EXCLUDED_PATHS = {
            "/public/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/**"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isExcludedPath(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(httpResponse);

        try {
            chain.doFilter(cachingRequest, cachingResponse);
        } finally {
            // Body 읽기
            String body = getRequestBody(cachingRequest);

            // Query String 포함 전체 URL
            String queryString = cachingRequest.getQueryString();
            String fullUrl = cachingRequest.getRequestURI() + (queryString != null ? "?" + queryString : "");

            logger.info("[{}] {}, status: {}, from IP: {}, Body: {}",
                    cachingRequest.getMethod(),
                    fullUrl,
                    cachingResponse.getStatus(),
                    cachingRequest.getRemoteAddr(),
                    body
            );

            // response body 도 비워주지 않으면 클라이언트가 응답을 못 받음!
            cachingResponse.copyBodyToResponse();
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                Object json = objectMapper.readValue(content, Object.class);
                return objectMapper.writeValueAsString(json);
            } catch (Exception e) {
                return new String(content, StandardCharsets.UTF_8)
                        .replaceAll("\n", "")
                        .replaceAll("\r", "");
            }
        }
        return "";
    }

    private boolean isExcludedPath(String requestURI) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestURI.equals(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}
