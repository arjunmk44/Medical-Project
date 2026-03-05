package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.repository.MlResultRepository;
import com.medicalml.service.MlOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MlController {

    private final MlOrchestrationService mlService;
    private final MlResultRepository mlResultRepository;

    @PostMapping("/analyze/{recordId}")
    public ResponseEntity<?> analyze(@PathVariable UUID recordId) {
        try {
            var result = mlService.analyzeRecord(recordId);
            return ResponseEntity.ok(ApiResponse.Success.builder()
                    .data(result).message("ML analysis complete").build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.Error.builder()
                    .error(ApiResponse.ErrorDetail.builder()
                            .code("ML_ANALYSIS_FAILED").message(e.getMessage()).build())
                    .build());
        }
    }

    @GetMapping("/results/{recordId}")
    public ResponseEntity<?> getResults(@PathVariable UUID recordId) {
        var results = mlResultRepository.findByRecordId(recordId);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(results).message("ML results retrieved").build());
    }

    @PostMapping("/retrain")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> retrain() {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Model retraining initiated (stub for local dev)").build());
    }
}
