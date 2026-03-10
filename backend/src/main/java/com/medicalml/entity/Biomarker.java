package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing biomarker readings from a patient checkup.
 * Maps to the "biomarkers" database table.
 *
 * Each biomarker record is linked to a CheckupRecord (via recordId)
 * and contains all clinical measurements taken during a single visit.
 */
@Entity
@Table(name = "biomarkers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Biomarker {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Foreign key linking to the CheckupRecord. */
    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    // ─── Vital & Cardiovascular ───────────────────────────────────────
    @Column(name = "systolic_bp", precision = 6, scale = 2)
    private BigDecimal systolicBp;

    @Column(name = "diastolic_bp", precision = 6, scale = 2)
    private BigDecimal diastolicBp;

    @Column(name = "heart_rate", precision = 6, scale = 2)
    private BigDecimal heartRate;

    // ─── Metabolic Panel ──────────────────────────────────────────────
    @Column(precision = 8, scale = 2)
    private BigDecimal glucose;

    @Column(precision = 5, scale = 2)
    private BigDecimal hba1c;

    @Column(name = "total_cholesterol", precision = 8, scale = 2)
    private BigDecimal totalCholesterol;

    @Column(precision = 8, scale = 2)
    private BigDecimal ldl;

    @Column(precision = 8, scale = 2)
    private BigDecimal hdl;

    @Column(precision = 8, scale = 2)
    private BigDecimal triglycerides;

    // ─── Organ Function (Liver) ───────────────────────────────────────
    @Column(precision = 8, scale = 2)
    private BigDecimal alt;

    @Column(precision = 8, scale = 2)
    private BigDecimal ast;

    @Column(precision = 8, scale = 2)
    private BigDecimal alp;

    // ─── Organ Function (Kidney) ──────────────────────────────────────
    @Column(precision = 6, scale = 3)
    private BigDecimal creatinine;

    @Column(precision = 8, scale = 2)
    private BigDecimal bun;

    @Column(precision = 8, scale = 2)
    private BigDecimal egfr;

    // ─── Anthropometric ───────────────────────────────────────────────
    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "waist_cm", precision = 6, scale = 2)
    private BigDecimal waistCm;

    @Column(name = "hip_cm", precision = 6, scale = 2)
    private BigDecimal hipCm;

    @Column(name = "waist_hip_ratio", precision = 4, scale = 3)
    private BigDecimal waistHipRatio;

    @Column(name = "skinfold_mm", precision = 6, scale = 2)
    private BigDecimal skinfoldMm;

    // ─── Lifestyle ────────────────────────────────────────────────────
    @Column(name = "smoking_status", length = 20)
    private String smokingStatus;

    @Column(name = "alcohol_units", precision = 5, scale = 1)
    private BigDecimal alcoholUnits;

    @Column(name = "activity_level", length = 20)
    private String activityLevel;

    @Column(name = "sleep_hours", precision = 4, scale = 1)
    private BigDecimal sleepHours;

    /** Timestamp when this record was created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
