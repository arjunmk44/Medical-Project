package com.medicalml.exception;

import com.medicalml.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for the entire application.
 *
 * @RestControllerAdvice — Catches exceptions thrown by any @RestController
 *                       and converts them into standardized JSON error
 *                       responses.
 *
 *                       This prevents raw stack traces from being exposed to
 *                       the client and ensures
 *                       all errors follow the ApiResponse.Error format
 *                       consistently.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handles all uncaught RuntimeExceptions.
         *
         * This is the catch-all handler for unexpected errors (e.g., "Patient not
         * found",
         * database connection failures, etc.). Returns HTTP 500 Internal Server Error.
         *
         * @param ex The RuntimeException that was thrown
         * @return 500 response with error code "INTERNAL_ERROR" and the exception
         *         message
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse.Error> handleRuntime(RuntimeException ex) {
                log.error("Runtime error: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.Error.builder()
                                                .error(ApiResponse.ErrorDetail.builder()
                                                                .code("INTERNAL_ERROR")
                                                                .message(ex.getMessage())
                                                                .build())
                                                .build());
        }

        /**
         * Handles validation errors from @Valid annotations on request DTOs.
         *
         * When a request body fails validation (e.g., missing required field like
         * "mrn"),
         * Spring throws MethodArgumentNotValidException. This handler collects all
         * field
         * errors and returns them as a list in the error details.
         *
         * Example error response:
         * { "error": { "code": "VALIDATION_ERROR", "details": ["firstName: First name
         * is required"] } }
         *
         * @param ex The validation exception containing field-level errors
         * @return 400 Bad Request with error code "VALIDATION_ERROR" and list of field
         *         errors
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse.Error> handleValidation(MethodArgumentNotValidException ex) {
                // Collect all field validation errors into a list of "fieldName: errorMessage"
                // strings
                var errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                                .body(ApiResponse.Error.builder()
                                                .error(ApiResponse.ErrorDetail.builder()
                                                                .code("VALIDATION_ERROR")
                                                                .message("Validation failed")
                                                                .details(errors)
                                                                .build())
                                                .build());
        }
}
