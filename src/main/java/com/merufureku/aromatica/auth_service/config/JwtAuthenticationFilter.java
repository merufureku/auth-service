package com.merufureku.aromatica.auth_service.config;

import com.merufureku.aromatica.auth_service.dto.token.ParsedTokenInfo;
import com.merufureku.aromatica.auth_service.helper.TokenHelper;
import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

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

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {

            Claims claims = tokenUtility.parseToken(token);
            ParsedTokenInfo parsedTokenInfo = tokenUtility.parseAndValidateToken(token);
            tokenHelper.validateToken(parsedTokenInfo.userId(), parsedTokenInfo.jti(), token);

            // Validate token
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            // Build authorities (ROLE_ prefix is important in Spring Security)
            Collection<GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            // Create authentication
            UsernamePasswordAuthenticationToken authentication =
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
