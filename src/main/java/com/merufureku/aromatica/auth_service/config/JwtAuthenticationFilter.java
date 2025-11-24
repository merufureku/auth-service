package com.merufureku.aromatica.auth_service.config;

import com.merufureku.aromatica.auth_service.helper.TokenHelper;
import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.ACCESS_TOKEN;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtility tokenUtility;
    private final TokenHelper tokenHelper;

    public JwtAuthenticationFilter(TokenUtility tokenUtility, TokenHelper tokenHelper) {
        this.tokenUtility = tokenUtility;
        this.tokenHelper = tokenHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getServletPath().equals("/auth/refresh/access-token")) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.substring(7);
        try {

            var claims = tokenUtility.parseToken(token, ACCESS_TOKEN);

            // Validate token
            var userId = claims.get("userId", Integer.class);
            var jti = claims.getId();
            var role = claims.get("role", String.class);

            tokenHelper.validateToken(userId, jti, token, ACCESS_TOKEN);

            // Build authorities (ROLE_ prefix is important in Spring Security)
            var authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            // Create authentication
            var authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // Set into security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
