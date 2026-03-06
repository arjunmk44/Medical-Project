package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.repository.MlResultRepository;
import com.medicalml.service.MlOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Machine Learning operations.
 *
 * Base path: /api/ml
 * Requires authentication. The /retrain endpoint additionally requires ADMIN
 * role.
 *
 * This controller provides endpoints to:
 * - Trigger ML analysis on a specific checkup record.
 * - Retrieve past ML analysis results.
 * - Initiate model retraining (admin-only).
 */
@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MlController {

    /**
     * Service that orchestrates the ML pipeline by calling the Python ML
     * microservice.
     */
    private final MlOrchestrationService mlService;

    /** Repository for querying stored ML analysis results from the database. */
    private final MlResultRepository mlResultRepository;

    /**
     * Run the full ML analysis pipeline on a single checkup record.
     *
     * Pipeline steps (executed by the Python ML microservice):
     * 1. Preprocessing — missing value imputation, outlier detection,
     * normalization.
     * 2. Dimensionality reduction — PCA and autoencoder embeddings.
     * 3. Clustering — K-Means, hierarchical, DBSCAN, GMM, LDA, PDM.
     * 4. Supervised prediction — ensemble of RF, XGBoost, LightGBM, SVM, LogReg.
     * 5. Interpretability — SHAP values and permutation importance.
     * 6. Risk scoring — composite score with health label and warnings.
     *
     * @param recordId UUID of the CheckupRecord to analyze
     * @return 200 OK with full ML results, or 500 Internal Server Error on failure
     */
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

    /**
     * Retrieve stored ML analysis results for a specific checkup record.
     *
     * Returns all past ML analyses (there may be multiple if the record was
     * re-analyzed after model retraining).
     *
     * @param recordId UUID of the CheckupRecord
     * @return 200 OK with list of MlAnalysisResult entities
     */
    @GetMapping("/results/{recordId}")
    public ResponseEntity<?> getResults(@PathVariable UUID recordId) {
        var results = mlResultRepository.findByRecordId(recordId);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(results).message("ML results retrieved").build());
    }

    /**
     * Trigger model retraining on the full dataset.
     * Restricted to ADMIN role via @PreAuthorize.
     *
     * Note: This is currently a stub for local development.
     * In production, this would pull all data from the database and retrain all
     * models.
     *
     * @return 200 OK with retraining status message
     */
    @PostMapping("/retrain")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> retrain() {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Model retraining initiated (stub for local dev)").build());
    }
}
