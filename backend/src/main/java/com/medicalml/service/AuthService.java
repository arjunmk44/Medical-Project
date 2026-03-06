package com.medicalml.service;

import com.medicalml.dto.AuthDto;
import com.medicalml.entity.User;
import com.medicalml.repository.UserRepository;
import com.medicalml.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service — handles user login and token refresh business logic.
 *
 * Login flow:
 * 1. Look up user by username in the database.
 * 2. Verify the account is active (not disabled).
 * 3. Compare the provided password against the stored BCrypt hash.
 * 4. Generate JWT access and refresh tokens.
 * 5. Return tokens with user metadata.
 *
 * Refresh flow:
 * 1. Validate the refresh token's signature and expiry.
 * 2. Extract the username from the token.
 * 3. Look up the user in the database (to get current role).
 * 4. Generate new access and refresh tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /** Repository for querying user credentials from the database. */
    private final UserRepository userRepository;

    /** JWT provider for generating and validating tokens. */
    private final JwtTokenProvider jwtTokenProvider;

    /** BCrypt password encoder for comparing plaintext passwords against hashes. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate a user and return JWT tokens.
     *
     * @param request LoginRequest containing username and password
     * @return AuthResponse with access token, refresh token, username, role, and
     *         expiry
     * @throws RuntimeException if username is not found, account is disabled,
     *                          or password doesn't match
     */
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // Step 1: Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Step 2: Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is disabled");
        }

        // Step 3: Verify password against BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Step 4: Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        log.info("User logged in: {}", user.getUsername());

        // Step 5: Build and return response
        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(900) // 900 seconds = 15 minutes
                .build();
    }

    /**
     * Refresh an expired access token using a valid refresh token.
     *
     * @param request RefreshRequest containing the refresh token
     * @return AuthResponse with new access token, new refresh token, username,
     *         role, and expiry
     * @throws RuntimeException if the refresh token is invalid/expired or user not
     *                          found
     */
    public AuthDto.AuthResponse refresh(AuthDto.RefreshRequest request) {
        // Step 1: Validate the refresh token
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Step 2: Extract username and look up user for current role
        String username = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 3: Generate new token pair (token rotation for security)
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(900)
                .build();
    }
}
