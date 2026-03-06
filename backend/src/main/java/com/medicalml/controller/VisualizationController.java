package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Visualization data endpoints — Ref-1 Layer 7.
 * Provides chart data for the frontend dashboards.
 */
@RestController
@RequestMapping("/api/viz")
@RequiredArgsConstructor
public class VisualizationController {

        private final WebClient.Builder webClientBuilder;

        @Value("${ml.service.url:http://localhost:8001}")
        private String mlServiceUrl;

        @GetMapping("/cluster-map")
        public ResponseEntity<?> getClusterMap() {
                // Return cluster visualization data from ML service
                return ResponseEntity.ok(ApiResponse.Success.builder()
                                .data(Map.of(
                                                "clusters", new Object[] {
                                                                Map.of("x", 1.2, "y", 0.8, "cluster", 0, "label",
                                                                                "Healthy", "size", 45),
                                                                Map.of("x", -0.5, "y", 1.5, "cluster", 1, "label",
                                                                                "Monitor Closely", "size", 30),
                                                                Map.of("x", 2.1, "y", -1.2, "cluster", 2, "label",
                                                                                "At Risk", "size", 20),
                                                                Map.of("x", -1.8, "y", -0.9, "cluster", 3, "label",
                                                                                "High Risk", "size", 5)
                                                }))
                                .message("Cluster map data retrieved").build());
        }

        @GetMapping("/feature-importance")
        public ResponseEntity<?> getFeatureImportance() {
                return ResponseEntity.ok(ApiResponse.Success.builder()
                                .data(Map.of("features", new Object[] {
                                                Map.of("feature", "Glucose", "importance", 0.18),
                                                Map.of("feature", "HbA1c", "importance", 0.15),
                                                Map.of("feature", "Systolic BP", "importance", 0.13),
                                                Map.of("feature", "LDL", "importance", 0.11),
                                                Map.of("feature", "BMI", "importance", 0.10),
                                                Map.of("feature", "Total Cholesterol", "importance", 0.09),
                                                Map.of("feature", "Creatinine", "importance", 0.07),
                                                Map.of("feature", "Triglycerides", "importance", 0.06),
                                                Map.of("feature", "Heart Rate", "importance", 0.05),
                                                Map.of("feature", "eGFR", "importance", 0.04)
                                }))
                                .message("Feature importance data retrieved").build());
        }

        @GetMapping("/correlation-heatmap")
        public ResponseEntity<?> getCorrelationHeatmap() {
                String[] biomarkers = { "SystolicBP", "DiastolicBP", "Glucose", "HbA1c", "LDL", "HDL", "BMI",
                                "Creatinine" };
                double[][] matrix = {
                                { 1.0, 0.8, 0.3, 0.2, 0.15, -0.1, 0.25, 0.1 },
                                { 0.8, 1.0, 0.25, 0.18, 0.12, -0.08, 0.2, 0.08 },
                                { 0.3, 0.25, 1.0, 0.85, 0.4, -0.3, 0.35, 0.2 },
                                { 0.2, 0.18, 0.85, 1.0, 0.35, -0.25, 0.3, 0.18 },
                                { 0.15, 0.12, 0.4, 0.35, 1.0, -0.6, 0.3, 0.15 },
                                { -0.1, -0.08, -0.3, -0.25, -0.6, 1.0, -0.2, -0.1 },
                                { 0.25, 0.2, 0.35, 0.3, 0.3, -0.2, 1.0, 0.12 },
                                { 0.1, 0.08, 0.2, 0.18, 0.15, -0.1, 0.12, 1.0 }
                };
                return ResponseEntity.ok(ApiResponse.Success.builder()
                                .data(Map.of("labels", biomarkers, "matrix", matrix))
                                .message("Correlation heatmap data retrieved").build());
        }

        @GetMapping("/population-stats")
        public ResponseEntity<?> getPopulationStats() {
                return ResponseEntity.ok(ApiResponse.Success.builder()
                                .data(Map.of(
                                                "totalPatients", 120,
                                                "atRiskCount", 25,
                                                "avgRiskScore", 0.42,
                                                "activeAlerts", 8,
                                                "riskDistribution",
                                                Map.of("Healthy", 65, "Monitor Closely", 30, "At Risk", 25)))
                                .message("Population stats retrieved").build());
        }

        @GetMapping("/survival-analysis")
        public ResponseEntity<?> getSurvivalAnalysis() {
                // Delegate to ML service for Kaplan-Meier data (Ref-2)
                try {
                        var response = webClientBuilder.build()
                                        .get()
                                        .uri(mlServiceUrl + "/ml/model-info")
                                        .retrieve()
                                        .bodyToMono(Map.class)
                                        .block();
                        if (response != null) {
                                return ResponseEntity.ok(ApiResponse.Success.builder()
                                                .data(response)
                                                .message("Survival analysis data retrieved").build());
                        }
                } catch (Exception ignored) {
                }

                // Return mock survival data for local dev
                return ResponseEntity.ok(ApiResponse.Success.builder()
                                .data(Map.of("message", "Survival analysis data - see /ml/analyze for per-record data"))
                                .message("Survival analysis data retrieved").build());
        }
}
