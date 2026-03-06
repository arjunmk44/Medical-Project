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

/**
 * REST controller for risk scores and health alert management.
 *
 * Base path: /api
 * Requires authentication.
 *
 * Provides endpoints for:
 * - Retrieving a patient's latest risk score from ML analysis.
 * - Listing unacknowledged health alerts across all patients.
 * - Acknowledging (dismissing) individual alerts.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RiskController {

    /**
     * Repository for querying ML analysis results (contains risk scores and health
     * labels).
     */
    private final MlResultRepository mlResultRepository;

    /** Repository for querying and updating early warning alerts. */
    private final EarlyWarningRepository warningRepository;

    /**
     * Get the latest ML risk score for a specific patient.
     *
     * Queries the most recent MlAnalysisResult across all of the patient's
     * checkup records. Returns null data if no analysis has been performed yet.
     *
     * @param patientId UUID of the patient
     * @return 200 OK with the latest MlAnalysisResult (or null if none exists)
     */
    @GetMapping("/risk/score/{patientId}")
    public ResponseEntity<?> getRiskScore(@PathVariable UUID patientId) {
        var latestResult = mlResultRepository.findLatestByPatientId(patientId);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(latestResult.orElse(null))
                .message("Risk score retrieved").build());
    }

    /**
     * List all unacknowledged (active) health alerts, sorted by most recent first.
     *
     * Alerts are generated during ML analysis when biomarker values exceed
     * clinical thresholds or the overall risk score is elevated.
     * Supports pagination via Spring's Pageable.
     *
     * @param pageable Pagination parameters (e.g., ?page=0&size=20)
     * @return 200 OK with paginated list of unacknowledged EarlyWarning entities
     */
    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(Pageable pageable) {
        var alerts = warningRepository.findByAcknowledgedFalseOrderByTriggeredAtDesc(pageable);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(alerts).message("Active alerts retrieved").build());
    }

    /**
     * Acknowledge (dismiss) a specific health alert.
     *
     * Sets the alert's 'acknowledged' flag to true and records the acknowledgment
     * timestamp. Acknowledged alerts no longer appear in the active alerts list.
     *
     * @param id UUID of the EarlyWarning alert to acknowledge
     * @return 200 OK on success, or RuntimeException if alert not found
     */
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
