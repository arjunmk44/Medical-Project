package com.medicalml.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Objects for authentication operations.
 *
 * Contains request/response DTOs for:
 * - LoginRequest: User login credentials
 * - AuthResponse: JWT token response after login/refresh
 * - RefreshRequest: Refresh token for obtaining new access tokens
 */
public class AuthDto {

    /**
     * Login request payload — sent by the frontend when the user submits the login
     * form.
     *
     * Both fields are validated with @NotBlank to ensure they are not null or
     * empty.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        /** Username for authentication. Must not be blank. */
        @NotBlank(message = "Username is required")
        private String username;

        /**
         * Password for authentication (plaintext — compared against BCrypt hash in DB).
         * Must not be blank.
         */
        @NotBlank(message = "Password is required")
        private String password;
    }

    /**
     * Authentication response — returned after successful login or token refresh.
     *
     * Contains both access and refresh tokens, along with user metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        /** JWT access token — include in Authorization header as "Bearer {token}". */
        private String accessToken;

        /**
         * JWT refresh token — use to obtain a new access token when the current one
         * expires.
         */
        private String refreshToken;

        /** Authenticated user's username. */
        private String username;

        /** User's role (e.g., "ADMIN", "USER"). */
        private String role;

        /** Access token expiry time in seconds (e.g., 900 = 15 minutes). */
        private long expiresIn;
    }

    /**
     * Refresh token request — sent when the frontend's access token has expired.
     *
     * The refresh token has a longer lifespan (default: 7 days) than the access
     * token (15 min).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {
        /** The refresh token previously received from login or a prior refresh call. */
        @NotBlank
        private String refreshToken;
    }
}
