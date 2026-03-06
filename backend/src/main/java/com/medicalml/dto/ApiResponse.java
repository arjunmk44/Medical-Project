package com.medicalml.dto;

import lombok.*;

/**
 * Standardized API response wrapper classes.
 *
 * Every API endpoint wraps its response in either ApiResponse.Success or
 * ApiResponse.Error
 * to provide a consistent JSON structure for the frontend to parse.
 *
 * Success response format:
 * {
 * "success": true,
 * "data": { ... }, // The actual response payload (generic type T)
 * "message": "Operation successful",
 * "timestamp": "2024-01-01T00:00:00Z"
 * }
 *
 * Error response format:
 * {
 * "success": false,
 * "error": {
 * "code": "ERROR_CODE", // Machine-readable error code
 * "message": "...", // Human-readable error description
 * "details": { ... } // Optional additional error details (e.g., validation
 * errors)
 * },
 * "timestamp": "2024-01-01T00:00:00Z"
 * }
 */
public class ApiResponse {

    /**
     * Generic success response wrapper.
     *
     * @param <T> The type of the data payload (e.g., PatientDto.Response, Map,
     *            List, etc.)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Success<T> {
        /** Always true for success responses. */
        @Builder.Default
        private boolean success = true;

        /** The response payload — can be any type (entity, list, map, etc.). */
        private T data;

        /** Human-readable message describing the operation result. */
        private String message;

        /** ISO-8601 timestamp of when the response was generated. */
        @Builder.Default
        private String timestamp = java.time.Instant.now().toString();
    }

    /**
     * Error response wrapper — used for all failed operations.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Error {
        /** Always false for error responses. */
        @Builder.Default
        private boolean success = false;

        /** Error detail object containing code, message, and optional details. */
        private ErrorDetail error;

        /** ISO-8601 timestamp of when the error occurred. */
        @Builder.Default
        private String timestamp = java.time.Instant.now().toString();
    }

    /**
     * Detailed error information.
     *
     * Examples:
     * - code: "AUTH_FAILED", message: "Invalid username or password"
     * - code: "VALIDATION_ERROR", message: "Validation failed", details: [field
     * errors]
     * - code: "INGEST_FAILED", message: "Unsupported file format"
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        /**
         * Machine-readable error code (e.g., AUTH_FAILED, VALIDATION_ERROR,
         * ML_ANALYSIS_FAILED).
         */
        private String code;

        /** Human-readable error message. */
        private String message;

        /** Optional extra details (e.g., list of field-level validation errors). */
        private Object details;
    }
}
