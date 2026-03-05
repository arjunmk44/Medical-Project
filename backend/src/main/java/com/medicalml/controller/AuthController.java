package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.dto.AuthDto;
import com.medicalml.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Logged out successfully").build());
    }
}
