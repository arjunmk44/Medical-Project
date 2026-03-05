package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.entity.EarlyWarning;
import com.medicalml.repository.EarlyWarningRepository;
import com.medicalml.repository.MlResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RiskController {

    private final MlResultRepository mlResultRepository;
    private final EarlyWarningRepository warningRepository;

    @GetMapping("/risk/score/{patientId}")
    public ResponseEntity<?> getRiskScore(@PathVariable UUID patientId) {
        // Get latest ML result for patient
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Risk score retrieved").build());
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(Pageable pageable) {
        var alerts = warningRepository.findByAcknowledgedFalseOrderByTriggeredAtDesc(pageable);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(alerts).message("Active alerts retrieved").build());
    }

    @PutMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable UUID id) {
        EarlyWarning warning = warningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
        warning.setAcknowledged(true);
        warning.setAcknowledgedAt(LocalDateTime.now());
        warningRepository.save(warning);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Alert acknowledged").build());
    }
}
