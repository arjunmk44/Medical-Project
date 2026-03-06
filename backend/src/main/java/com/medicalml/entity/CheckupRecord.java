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

/**
 * JPA entity representing a single checkup/visit record for a patient.
 * Maps to the "checkup_records" database table.
 *
 * Each record is created when medical data is ingested (uploaded via
 * CSV/Excel/JSON).
 * A CheckupRecord links a patient to their biomarker data and ML analysis
 * results.
 *
 * Processing status flow:
 * UPLOADED → PARSED → ANALYZED
 */
@Entity
@Table(name = "checkup_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupRecord {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Foreign key linking this record to the Patient. */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Date of the checkup/visit. */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /** Format of the original uploaded file: "CSV", "EXCEL", or "JSON". */
    @Column(name = "source_format", nullable = false, length = 20)
    private String sourceFormat;

    /**
     * Raw data from the uploaded file stored as JSONB.
     * Preserves the original key-value pairs for audit and reprocessing.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json", columnDefinition = "jsonb")
    private Map<String, Object> rawJson;

    /**
     * Current processing status:
     * - "UPLOADED" — File received but not yet parsed.
     * - "PARSED" — Data extracted and biomarker records created.
     * - "ANALYZED" — ML pipeline has been run on this record.
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "UPLOADED";

    /** Timestamp when this record was first created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
