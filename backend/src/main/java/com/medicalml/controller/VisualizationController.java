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
 * Provides pre-computed chart data for the frontend dashboards.
 *
 * Base path: /api/viz
 * Requires authentication.
 *
 * These endpoints return chart-ready data structures for:
 * - Patient cluster scatter plots
 * - Feature importance bar charts
 * - Biomarker correlation heatmaps
 * - Population-level statistics
 * - Survival analysis curves
 *
 * Note: In the current implementation, most endpoints return mock/demo data.
 * In production, these would pull from the database or the ML service.
 */
@RestController
@RequestMapping("/api/viz")
@RequiredArgsConstructor
public class VisualizationController {

        /** WebClient builder for making HTTP calls to the ML microservice. */
        private final WebClient.Builder webClientBuilder;

        /** URL of the Python ML microservice (defaults to http://localhost:8001). */
        @Value("${ml.service.url:http://localhost:8001}")
        private String mlServiceUrl;

        /**
         * Get patient cluster map data for a 2D scatter plot.
         *
         * Returns cluster centers with:
         * - x, y: Coordinates (from PCA or t-SNE dimensionality reduction)
         * - cluster: Cluster ID (0-3)
         * - label: Human-readable cluster name (Healthy, Monitor Closely, At Risk, High
         * Risk)
         * - size: Number of patients in the cluster
         *
         * @return 200 OK with cluster map data
         */
        @GetMapping("/cluster-map")
        public ResponseEntity<?> getClusterMap() {
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

        /**
         * Get feature importance data for a bar chart.
         *
         * Returns the top 10 most important biomarker features ranked by their
         * contribution to the ML model's predictions (from SHAP analysis).
         *
         * @return 200 OK with feature names and importance scores (0.0 to 1.0)
         */
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

        /**
         * Get correlation heatmap data showing relationships between biomarkers.
         *
         * Returns:
         * - labels: Array of biomarker names (row and column headers)
         * - matrix: 2D array of Pearson correlation coefficients (-1.0 to 1.0)
         *
         * Values close to 1.0 indicate strong positive correlation,
         * close to -1.0 indicate strong negative correlation,
         * and close to 0.0 indicate no linear relationship.
         *
         * @return 200 OK with labels and correlation matrix
         */
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

        /**
         * Get population-level health statistics for the dashboard summary cards.
         *
         * Returns:
         * - totalPatients: Total number of patients in the system
         * - atRiskCount: Number of patients classified as "At Risk"
         * - avgRiskScore: Average risk score across all patients (0.0 to 1.0)
         * - activeAlerts: Number of unacknowledged health warnings
         * - riskDistribution: Breakdown of patients by health label
         *
         * @return 200 OK with population statistics
         */
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

        /**
         * Get survival analysis data (Kaplan-Meier curves) for patient subgroups.
         *
         * First attempts to fetch data from the Python ML service. If the ML service
         * is unavailable, falls back to returning a stub message.
         *
         * In production, this would return Kaplan-Meier survival curves per PDM
         * subgroup,
         * log-rank test results, and demographic breakdowns (Ref-2 Section 3.3.2).
         *
         * @return 200 OK with survival analysis data (from ML service or stub)
         */
        @GetMapping("/survival-analysis")
        public ResponseEntity<?> getSurvivalAnalysis() {
                // Attempt to delegate to the Python ML service for real survival data
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
                        // ML service unavailable — fall back to stub data
                }

                // Return mock survival data for local development
                return ResponseEntity.ok(ApiResponse.Success.builder()
                                .data(Map.of("message", "Survival analysis data - see /ml/analyze for per-record data"))
                                .message("Survival analysis data retrieved").build());
        }
}
