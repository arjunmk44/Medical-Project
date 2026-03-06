package com.medicalml.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter — runs once per request before reaching any
 * controller.
 *
 * Extends OncePerRequestFilter to guarantee single execution per request
 * (prevents double-filtering in forwarded/dispatched requests).
 *
 * Processing flow:
 * 1. Extracts the JWT token from the "Authorization: Bearer {token}" header.
 * 2. Validates the token's signature and expiry using JwtTokenProvider.
 * 3. If valid: extracts username and role, creates a Spring Authentication
 * object,
 * and stores it in SecurityContextHolder (making the user "logged in" for this
 * request).
 * 4. If invalid or missing: the request proceeds without authentication
 * (Spring Security will block access to protected endpoints).
 *
 * This filter is inserted before UsernamePasswordAuthenticationFilter in
 * SecurityConfig.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Provider that handles JWT creation, parsing, and validation. */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Core filter logic — processes every incoming HTTP request.
     *
     * @param request     The incoming HTTP request
     * @param response    The HTTP response (not modified by this filter)
     * @param filterChain The chain of remaining filters and the target controller
     * @throws ServletException If a servlet error occurs
     * @throws IOException      If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract token from "Authorization: Bearer ..." header
        String token = extractToken(request);

        // Step 2-3: If token is present and valid, authenticate the user for this
        // request
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            if (role != null) {
                // Create granted authority with "ROLE_" prefix (required by Spring Security's
                // hasRole())
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // Create authentication token (password=null since we're using JWT, not
                // credentials)
                var authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                // Store in SecurityContext — makes the user "authenticated" for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Step 4: Continue the filter chain (pass to next filter or controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the JWT token from the Authorization header.
     *
     * Expected format: "Bearer eyJhbGciOiJSUzI1NiJ9.eyJ..."
     * Returns just the token part (after "Bearer ").
     *
     * @param request The HTTP request
     * @return The JWT token string, or null if not present/malformed
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
