package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "biomarkers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Biomarker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    // Vital & Cardiovascular
    @Column(name = "systolic_bp")
    private BigDecimal systolicBp;

    @Column(name = "diastolic_bp")
    private BigDecimal diastolicBp;

    @Column(name = "heart_rate")
    private BigDecimal heartRate;

    // Metabolic Panel
    private BigDecimal glucose;
    private BigDecimal hba1c;

    @Column(name = "total_cholesterol")
    private BigDecimal totalCholesterol;

    private BigDecimal ldl;
    private BigDecimal hdl;
    private BigDecimal triglycerides;

    // Organ Function (Liver)
    private BigDecimal alt;
    private BigDecimal ast;
    private BigDecimal alp;

    // Organ Function (Kidney)
    private BigDecimal creatinine;
    private BigDecimal bun;
    private BigDecimal egfr;

    // Anthropometric
    @Column(name = "height_cm")
    private BigDecimal heightCm;

    @Column(name = "weight_kg")
    private BigDecimal weightKg;

    private BigDecimal bmi;

    @Column(name = "waist_cm")
    private BigDecimal waistCm;

    @Column(name = "hip_cm")
    private BigDecimal hipCm;

    @Column(name = "waist_hip_ratio")
    private BigDecimal waistHipRatio;

    @Column(name = "skinfold_mm")
    private BigDecimal skinfoldMm;

    // Lifestyle
    @Column(name = "smoking_status", length = 20)
    private String smokingStatus;

    @Column(name = "alcohol_units")
    private BigDecimal alcoholUnits;

    @Column(name = "activity_level", length = 20)
    private String activityLevel;

    @Column(name = "sleep_hours")
    private BigDecimal sleepHours;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
