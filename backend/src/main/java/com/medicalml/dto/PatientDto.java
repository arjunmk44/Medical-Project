package com.medicalml.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Objects for Patient-related API operations.
 *
 * Contains:
 * - CreateRequest: Input DTO for creating/updating patients
 * - Response: Basic patient info for list views
 * - DetailResponse: Full patient profile with checkup history and warnings
 * - CheckupSummary: Summary of a single checkup record
 * - WarningResponse: An early warning alert for a patient
 */
public class PatientDto {

    /**
     * Request body for creating or updating a patient.
     *
     * Required fields are validated with @NotBlank.
     * Optional fields (age, sex, dateOfBirth) can be null.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        /**
         * Medical Record Number — unique identifier for the patient in the hospital
         * system.
         */
        @NotBlank(message = "MRN is required")
        private String mrn;

        /** Patient's first name. */
        @NotBlank(message = "First name is required")
        private String firstName;

        /** Patient's last name. */
        @NotBlank(message = "Last name is required")
        private String lastName;

        /** Patient's age in years (optional). */
        private Integer age;

        /** Patient's sex — single character: "M" (Male) or "F" (Female) (optional). */
        private String sex;

        /** Patient's date of birth (optional). */
        private LocalDate dateOfBirth;
    }

    /**
     * Basic patient response DTO — used in list views (patient list page).
     *
     * Contains demographics plus the latest risk assessment from ML analysis.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        /** Unique patient identifier. */
        private UUID id;

        /** Medical Record Number. */
        private String mrn;

        /** Patient's first name. */
        private String firstName;

        /** Patient's last name. */
        private String lastName;

        /** Patient's age in years. */
        private Integer age;

        /** Patient's sex (M/F). */
        private String sex;

        /** Patient's date of birth. */
        private LocalDate dateOfBirth;

        /** Latest health label from ML analysis (e.g., "Healthy", "At Risk"). */
        private String latestRiskLabel;

        /**
         * Latest risk score from ML analysis (0.0 = lowest risk, 1.0 = highest risk).
         */
        private BigDecimal latestRiskScore;

        /** Timestamp when the patient record was created. */
        private LocalDateTime createdAt;
    }

    /**
     * Detailed patient response DTO — used on the patient profile page.
     *
     * Extends the basic response with full checkup history and active warnings.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResponse {
        /** Unique patient identifier. */
        private UUID id;

        /** Medical Record Number. */
        private String mrn;

        /** Patient's first name. */
        private String firstName;

        /** Patient's last name. */
        private String lastName;

        /** Patient's age in years. */
        private Integer age;

        /** Patient's sex (M/F). */
        private String sex;

        /** Patient's date of birth. */
        private LocalDate dateOfBirth;

        /** Latest health label from ML analysis. */
        private String latestRiskLabel;

        /** Latest risk score from ML analysis (0.0 to 1.0). */
        private BigDecimal latestRiskScore;

        /**
         * List of all past checkup records, ordered by date descending (most recent
         * first).
         */
        private List<CheckupSummary> checkupHistory;

        /** List of active (unacknowledged) early warning alerts for this patient. */
        private List<WarningResponse> activeWarnings;

        /** Timestamp when the patient record was created. */
        private LocalDateTime createdAt;
    }

    /**
     * Summary of a single checkup record — used within the patient's checkup
     * history.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckupSummary {
        /** UUID of the checkup record. */
        private UUID recordId;

        /** Date the checkup was performed. */
        private LocalDate recordDate;

        /** Current processing status (UPLOADED, PARSED, ANALYZED). */
        private String status;

        /** Health label assigned by ML analysis (null if not yet analyzed). */
        private String healthLabel;

        /** Risk score from ML analysis (null if not yet analyzed). */
        private BigDecimal riskScore;
    }

    /**
     * Early warning alert DTO — displayed on the patient profile and dashboard.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarningResponse {
        /** Unique warning identifier. */
        private UUID id;

        /** Type of warning (e.g., "HIGH_RISK_SCORE", "BIOMARKER_DEVIATION_GLUCOSE"). */
        private String warningType;

        /** Severity level: "LOW", "MEDIUM", or "HIGH". */
        private String severity;

        /** Human-readable warning message describing the issue. */
        private String message;

        /** Recommended action for the clinician. */
        private String recommendation;

        /** Timestamp when the warning was generated. */
        private LocalDateTime triggeredAt;

        /** Whether a clinician has acknowledged/dismissed this warning. */
        private Boolean acknowledged;
    }
}
