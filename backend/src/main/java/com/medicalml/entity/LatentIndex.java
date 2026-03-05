package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "latent_indices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LatentIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pca_index", columnDefinition = "jsonb")
    private Map<String, Object> pcaIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "autoencoder_embedding", columnDefinition = "jsonb")
    private Map<String, Object> autoencoderEmbedding;

    @Column(name = "metabolic_composite", precision = 8, scale = 4)
    private BigDecimal metabolicComposite;

    @Column(name = "cardiovascular_composite", precision = 8, scale = 4)
    private BigDecimal cardiovascularComposite;

    @Column(name = "organ_function_composite", precision = 8, scale = 4)
    private BigDecimal organFunctionComposite;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
