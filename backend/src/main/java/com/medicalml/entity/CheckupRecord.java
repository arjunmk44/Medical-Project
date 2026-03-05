package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "checkup_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckupRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "source_format", nullable = false, length = 20)
    private String sourceFormat;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json", columnDefinition = "jsonb")
    private Map<String, Object> rawJson;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "UPLOADED";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
