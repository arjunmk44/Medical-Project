package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ml_analysis_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MlAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "health_label", length = 50)
    private String healthLabel;

    @Column(name = "risk_score", precision = 5, scale = 4)
    private BigDecimal riskScore;

    @Column(name = "cluster_id")
    private Integer clusterId;

    @Column(name = "cluster_label", length = 100)
    private String clusterLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lda_topic_distribution", columnDefinition = "jsonb")
    private Map<String, Object> ldaTopicDistribution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pdm_topic_distribution", columnDefinition = "jsonb")
    private Map<String, Object> pdmTopicDistribution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pca_components", columnDefinition = "jsonb")
    private Map<String, Object> pcaComponents;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shap_values", columnDefinition = "jsonb")
    private Map<String, Object> shapValues;

    @Column(name = "analyzed_at", nullable = false)
    @Builder.Default
    private LocalDateTime analyzedAt = LocalDateTime.now();
}
