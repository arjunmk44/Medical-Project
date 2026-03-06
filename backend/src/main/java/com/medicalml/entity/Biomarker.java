package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity storing biomarker measurements extracted from a patient's checkup
 * data.
 * Maps to the "biomarkers" database table.
 *
 * Each Biomarker row is linked to a single CheckupRecord (via recordId).
 * Contains numeric values for vital signs, metabolic panel, organ function
 * markers, anthropometric measurements, and lifestyle factors.
 *
 * All biomarker fields are nullable — a patient may not have every test
 * performed.
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

    /** Foreign key linking this biomarker set to a CheckupRecord. */
    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    // ====== Vital & Cardiovascular Biomarkers ======

    /** Systolic blood pressure in mmHg (e.g., 120). Normal: <120. */
    @Column(name = "systolic_bp")
    private BigDecimal systolicBp;

    /** Diastolic blood pressure in mmHg (e.g., 80). Normal: <80. */
    @Column(name = "diastolic_bp")
    private BigDecimal diastolicBp;

    /** Resting heart rate in beats per minute (e.g., 72). Normal: 60-100. */
    @Column(name = "heart_rate")
    private BigDecimal heartRate;

    // ====== Metabolic Panel ======

    /**
     * Fasting blood glucose in mg/dL (e.g., 100). Normal: <100, Prediabetic:
     * 100-125, Diabetic: ≥126.
     */
    private BigDecimal glucose;

    /**
     * Glycated hemoglobin percentage (e.g., 5.5). Normal: <5.7, Prediabetic:
     * 5.7-6.4, Diabetic: ≥6.5.
     */
    private BigDecimal hba1c;

    /** Total cholesterol in mg/dL (e.g., 200). Desirable: <200. */
    @Column(name = "total_cholesterol")
    private BigDecimal totalCholesterol;

    /** Low-density lipoprotein ("bad" cholesterol) in mg/dL. Optimal: <100. */
    private BigDecimal ldl;

    /** High-density lipoprotein ("good" cholesterol) in mg/dL. Desirable: ≥60. */
    private BigDecimal hdl;

    /** Triglycerides in mg/dL. Normal: <150. */
    private BigDecimal triglycerides;

    // ====== Organ Function — Liver ======

    /** Alanine aminotransferase (liver enzyme) in U/L. Normal: 7-56. */
    private BigDecimal alt;

    /** Aspartate aminotransferase (liver enzyme) in U/L. Normal: 10-40. */
    private BigDecimal ast;

    /** Alkaline phosphatase (liver/bone enzyme) in U/L. Normal: 44-147. */
    private BigDecimal alp;

    // ====== Organ Function — Kidney ======

    /** Serum creatinine in mg/dL. Normal: 0.7-1.3 (males), 0.6-1.1 (females). */
    private BigDecimal creatinine;

    /** Blood urea nitrogen in mg/dL. Normal: 7-20. */
    private BigDecimal bun;

    /** Estimated glomerular filtration rate in mL/min/1.73m². Normal: >90. */
    private BigDecimal egfr;

    // ====== Anthropometric Measurements ======

    /** Patient height in centimeters. */
    @Column(name = "height_cm")
    private BigDecimal heightCm;

    /** Patient weight in kilograms. */
    @Column(name = "weight_kg")
    private BigDecimal weightKg;

    /**
     * Body Mass Index (weight/height²). Normal: 18.5-24.9, Overweight: 25-29.9,
     * Obese: ≥30.
     */
    private BigDecimal bmi;

    /**
     * Waist circumference in centimeters. Risk: >102cm (males), >88cm (females).
     */
    @Column(name = "waist_cm")
    private BigDecimal waistCm;

    /** Hip circumference in centimeters. */
    @Column(name = "hip_cm")
    private BigDecimal hipCm;

    /** Waist-to-hip ratio. Risk: >0.90 (males), >0.85 (females). */
    @Column(name = "waist_hip_ratio")
    private BigDecimal waistHipRatio;

    /** Skinfold thickness measurement in millimeters (body fat estimation). */
    @Column(name = "skinfold_mm")
    private BigDecimal skinfoldMm;

    // ====== Lifestyle Factors ======

    /** Smoking status: "NEVER", "FORMER", or "CURRENT". */
    @Column(name = "smoking_status", length = 20)
    private String smokingStatus;

    /** Weekly alcohol consumption in standard units. */
    @Column(name = "alcohol_units")
    private BigDecimal alcoholUnits;

    /**
     * Physical activity level: "SEDENTARY", "LIGHT", "MODERATE", "ACTIVE",
     * "VERY_ACTIVE".
     */
    @Column(name = "activity_level", length = 20)
    private String activityLevel;

    /** Average daily sleep duration in hours. Recommended: 7-9 hours. */
    @Column(name = "sleep_hours")
    private BigDecimal sleepHours;

    /** Timestamp when this biomarker record was created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
