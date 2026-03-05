package com.medicalml.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AuthDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String username;
        private String role;
        private long expiresIn;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }
}
