package com.medicalml.exception;

import com.medicalml.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse.Error> handleValidation(MethodArgumentNotValidException ex) {
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
