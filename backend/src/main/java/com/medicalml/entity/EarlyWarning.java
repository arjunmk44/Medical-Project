package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "early_warnings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EarlyWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "record_id")
    private UUID recordId;

    @Column(name = "warning_type", nullable = false, length = 100)
    private String warningType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "triggered_at", nullable = false)
    @Builder.Default
    private LocalDateTime triggeredAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
}
