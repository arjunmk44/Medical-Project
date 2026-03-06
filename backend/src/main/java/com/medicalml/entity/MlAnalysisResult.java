package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity storing the results of ML analysis for a single checkup record.
 * Maps to the "ml_analysis_results" database table.
 *
 * Created after the full ML pipeline runs on a patient's biomarkers:
 * - Health label classification (Healthy / Monitor Closely / At Risk)
 * - Numerical risk score (0.0 to 1.0)
 * - Cluster assignment from unsupervised learning
 * - Topic distributions from LDA and PDM models
 * - PCA components and SHAP feature importance values
 *
 * Each record may have multiple MlAnalysisResult entries if re-analyzed
 * after model retraining.
 */
@Entity
@Table(name = "ml_analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlAnalysisResult {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Foreign key linking to the CheckupRecord that was analyzed. */
    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    /**
     * Name of the ML model/pipeline that produced this result (e.g., "ensemble").
     */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    /** Predicted health category: "Healthy", "Monitor Closely", or "At Risk". */
    @Column(name = "health_label", length = 50)
    private String healthLabel;

    /** Overall risk score from 0.0 (lowest risk) to 1.0 (highest risk). */
    @Column(name = "risk_score", precision = 5, scale = 4)
    private BigDecimal riskScore;

    /** Cluster ID from K-Means clustering (0-3). */
    @Column(name = "cluster_id")
    private Integer clusterId;

    /** Human-readable cluster label (e.g., "Healthy", "High Risk"). */
    @Column(name = "cluster_label", length = 100)
    private String clusterLabel;

    /**
     * LDA topic distribution as JSONB — per-patient topic probabilities (Ref-2).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lda_topic_distribution", columnDefinition = "jsonb")
    private Map<String, Object> ldaTopicDistribution;

    /**
     * PDM topic distribution as JSONB — age/sex-corrected topic probabilities
     * (Ref-2).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pdm_topic_distribution", columnDefinition = "jsonb")
    private Map<String, Object> pdmTopicDistribution;

    /** PCA principal components as JSONB — dimensionality reduction results. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pca_components", columnDefinition = "jsonb")
    private Map<String, Object> pcaComponents;

    /** SHAP values as JSONB — per-feature contribution to the prediction. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shap_values", columnDefinition = "jsonb")
    private Map<String, Object> shapValues;

    /** Timestamp when this analysis was performed. Defaults to now. */
    @Column(name = "analyzed_at", nullable = false)
    @Builder.Default
    private LocalDateTime analyzedAt = LocalDateTime.now();
}
