package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.dto.AuthDto;
import com.medicalml.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations (login, token refresh, logout).
 *
 * Base path: /api/auth
 * All endpoints in this controller are publicly accessible (no JWT required)
 * as configured in SecurityConfig.
 *
 * @RestController — Combines @Controller + @ResponseBody; methods return JSON
 *                 directly.
 * @RequestMapping — Sets the base URL path for all endpoints in this class.
 * @RequiredArgsConstructor — Lombok: auto-generates constructor for final
 *                          fields (dependency injection).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Service layer handling authentication logic (credential validation, JWT
     * generation).
     */
    private final AuthService authService;

    /**
     * Authenticates a user with username and password.
     *
     * Flow:
     * 1. Validates the request body (username & password must not be blank).
     * 2. Delegates to AuthService.login() which verifies credentials against the
     * database.
     * 3. On success: returns JWT access token, refresh token, username, and role.
     * 4. On failure: returns HTTP 401 with error code "AUTH_FAILED".
     *
     * @param request LoginRequest DTO containing username and password
     * @return 200 OK with tokens on success, or 401 Unauthorized on failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            var response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.Success.builder()
                    .data(response).message("Login successful").build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.Error.builder()
                    .error(ApiResponse.ErrorDetail.builder()
                            .code("AUTH_FAILED").message(e.getMessage()).build())
                    .build());
        }
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * Flow:
     * 1. Validates the refresh token from the request body.
     * 2. Delegates to AuthService.refresh() which validates the token and issues
     * new ones.
     * 3. On success: returns new access/refresh token pair.
     * 4. On failure: returns HTTP 401 with error code "REFRESH_FAILED".
     *
     * @param request RefreshRequest DTO containing the refresh token
     * @return 200 OK with new tokens on success, or 401 Unauthorized on failure
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody AuthDto.RefreshRequest request) {
        try {
            var response = authService.refresh(request);
            return ResponseEntity.ok(ApiResponse.Success.builder()
                    .data(response).message("Token refreshed").build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.Error.builder()
                    .error(ApiResponse.ErrorDetail.builder()
                            .code("REFRESH_FAILED").message(e.getMessage()).build())
                    .build());
        }
    }

    /**
     * Logs out the current user.
     *
     * Note: Since JWT is stateless, server-side logout simply returns a success
     * message.
     * The client is responsible for discarding the stored tokens.
     * In a production system, you might implement token blacklisting here.
     *
     * @return 200 OK with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Logged out successfully").build());
    }
}
