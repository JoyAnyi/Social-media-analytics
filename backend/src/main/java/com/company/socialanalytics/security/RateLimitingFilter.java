package com.company.socialanalytics.security;

import com.company.socialanalytics.common.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final SecurityProperties securityProperties;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(SecurityProperties securityProperties, Clock clock, ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Limit limit = limitFor(request);
        if (limit != null && !allow(request, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    ApiError.of(429, "Too Many Requests", "Too many requests. Please wait before trying again.",
                            request.getRequestURI()));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean allow(HttpServletRequest request, Limit limit) {
        Instant now = clock.instant();
        String key = limit.name() + ":" + clientIp(request);
        WindowCounter counter = counters.compute(key, (ignored, existing) -> {
            if (existing == null || !now.isBefore(existing.expiresAt())) {
                return new WindowCounter(1, now.plus(securityProperties.getRateLimitWindow()));
            }
            existing.increment();
            return existing;
        });
        return counter.count() <= limit.maxRequests();
    }

    private Limit limitFor(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (path.equals("/api/v1/auth/login")) {
            return new Limit("login", securityProperties.getLoginRateLimit());
        }
        if (path.equals("/api/v1/auth/register")) {
            return new Limit("register", securityProperties.getRegistrationRateLimit());
        }
        if (path.contains("/password-reset")) {
            return new Limit("password-reset", securityProperties.getPasswordResetRateLimit());
        }
        if (path.startsWith("/api/v1/reports")) {
            return new Limit("reports", securityProperties.getReportGenerationRateLimit());
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Limit(String name, int maxRequests) {
    }

    private static class WindowCounter {
        private int count;
        private final Instant expiresAt;

        WindowCounter(int count, Instant expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }

        void increment() {
            count++;
        }

        int count() {
            return count;
        }

        Instant expiresAt() {
            return expiresAt;
        }
    }
}
