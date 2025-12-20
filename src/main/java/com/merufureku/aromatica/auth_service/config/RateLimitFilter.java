package com.merufureku.aromatica.auth_service.config;

import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.ACCESS_TOKEN;
import static com.merufureku.aromatica.auth_service.constants.AuthConstants.REFRESH_TOKEN;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LogManager.getLogger(RateLimitFilter.class);

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final TokenUtility tokenUtility;

    // Rate limit configurations per endpoint pattern
    private static final Map<String, RateLimitConfig> ENDPOINT_LIMITS = Map.of(
            "/login", new RateLimitConfig(5, Duration.ofMinutes(1), RateLimitScope.IP),
            "/register", new RateLimitConfig(3, Duration.ofMinutes(5), RateLimitScope.IP),
            "/auth/refresh", new RateLimitConfig(10, Duration.ofMinutes(1), RateLimitScope.IP),
            "/auth/me/change-password", new RateLimitConfig(3, Duration.ofMinutes(5), RateLimitScope.USER)
    );

    public RateLimitFilter(TokenUtility tokenUtility) {
        this.tokenUtility = tokenUtility;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI()
                .replace(request.getContextPath(), "");

        RateLimitConfig config = findMatchingConfig(path);

        if (config != null) {
            String key = resolveKey(request, path, config.scope);
            Bucket bucket = resolveBucket(key, config);

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                logger.warn("Rate limit exceeded: key={}, path={}", key, path);
                sendRateLimitError(response, request.getRequestURI());
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private RateLimitConfig findMatchingConfig(String path) {
        for (Map.Entry<String, RateLimitConfig> entry : ENDPOINT_LIMITS.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveKey(HttpServletRequest request, String path, RateLimitScope scope) {
        return switch (scope) {
            case IP -> "ip:" + getClientIP(request) + ":" + path;
            case USER -> {
                String userId = extractUserId(request);
                yield userId != null ? "user:" + userId + ":" + path
                        : "ip:" + getClientIP(request) + ":" + path; // Fallback to IP
            }
            case GLOBAL -> "global:" + path; // All users share same limit
        };
    }

    private String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String tokenType = request.getRequestURI().contains("/auth/refresh") ? REFRESH_TOKEN : ACCESS_TOKEN;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                var claims = tokenUtility.parseToken(token, tokenType);
                Integer userId = claims.get("userId", Integer.class);
                return userId != null ? userId.toString() : null;
            } catch (Exception e) {
                logger.debug("Failed to extract userId from token: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    private Bucket resolveBucket(String key, RateLimitConfig config) {
        return cache.computeIfAbsent(key, k -> createBucket(config));
    }

    private Bucket createBucket(RateLimitConfig config) {
        Bandwidth limit = Bandwidth.classic(
                config.capacity,
                Refill.intervally(config.capacity, config.duration)
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        String xrfHeader = request.getHeader("X-Real-IP");
        if (xrfHeader != null && !xrfHeader.isEmpty()) {
            return xrfHeader;
        }

        return request.getRemoteAddr();
    }

    private void sendRateLimitError(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                String.format(
                        "{\"status\":429,\"error\":\"Too Many Requests\"," +
                                "\"message\":\"Rate limit exceeded. Please try again later.\"," +
                                "\"path\":\"%s\"}",
                        path
                )
        );
    }

    /**
     * Rate limiting scope determines what is being limited
     */
    private enum RateLimitScope {
        IP,      // Limit per IP address (for unauthenticated endpoints)
        USER,    // Limit per user ID (for authenticated endpoints)
        GLOBAL   // Limit total requests to endpoint (all users combined)
    }

    private record RateLimitConfig(long capacity, Duration duration, RateLimitScope scope) {}
}