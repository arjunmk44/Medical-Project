package com.medicalml.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PatientDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "MRN is required")
        private String mrn;
        @NotBlank(message = "First name is required")
        private String firstName;
        @NotBlank(message = "Last name is required")
        private String lastName;
        private Integer age;
        private String sex;
        private LocalDate dateOfBirth;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String mrn;
        private String firstName;
        private String lastName;
        private Integer age;
        private String sex;
        private LocalDate dateOfBirth;
        private String latestRiskLabel;
        private BigDecimal latestRiskScore;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResponse {
        private UUID id;
        private String mrn;
        private String firstName;
        private String lastName;
        private Integer age;
        private String sex;
        private LocalDate dateOfBirth;
        private String latestRiskLabel;
        private BigDecimal latestRiskScore;
        private List<CheckupSummary> checkupHistory;
        private List<WarningResponse> activeWarnings;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckupSummary {
        private UUID recordId;
        private LocalDate recordDate;
        private String status;
        private String healthLabel;
        private BigDecimal riskScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarningResponse {
        private UUID id;
        private String warningType;
        private String severity;
        private String message;
        private String recommendation;
        private LocalDateTime triggeredAt;
        private Boolean acknowledged;
    }
}
