package com.medicalml.dto;

import lombok.*;
import java.util.Map;

public class ApiResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Success<T> {
        @Builder.Default
        private boolean success = true;
        private T data;
        private String message;
        @Builder.Default
        private String timestamp = java.time.Instant.now().toString();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Error {
        @Builder.Default
        private boolean success = false;
        private ErrorDetail error;
        @Builder.Default
        private String timestamp = java.time.Instant.now().toString();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        private String code;
        private String message;
        private Object details;
    }
}
