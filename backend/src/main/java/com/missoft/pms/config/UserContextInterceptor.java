package com.missoft.pms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = resolveUserId(request);
        if (userId != null) {
            logger.debug("Captured User ID: {} for request: {}", userId, request.getRequestURI());
        } else {
            logger.debug("No User ID found for request: {}", request.getRequestURI());
        }
        UserContextHolder.setCurrentUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private Long resolveUserId(HttpServletRequest request) {
        Long fromAuth = parseFromAuthorization(request.getHeader("Authorization"));
        if (fromAuth != null) {
            return fromAuth;
        }
        return parseLong(request.getHeader("X-User-Id"));
    }

    private Long parseFromAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        if (token.startsWith("simple_token_")) {
            String[] parts = token.split("_");
            if (parts.length >= 3) {
                return parseLong(parts[2]);
            }
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
