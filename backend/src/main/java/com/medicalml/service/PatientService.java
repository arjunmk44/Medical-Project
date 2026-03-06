package com.medicalml.service;

import com.medicalml.dto.PatientDto;
import com.medicalml.entity.*;
import com.medicalml.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Patient service — handles all patient-related business logic.
 *
 * Provides operations for:
 * - Listing patients (with optional search and pagination)
 * - Retrieving detailed patient profiles (with checkup history and warnings)
 * - Creating new patients
 * - Updating existing patients
 * - Soft-deleting patients (sets isDeleted=true)
 *
 * All read operations filter out soft-deleted patients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

        private final PatientRepository patientRepository;
        private final CheckupRecordRepository checkupRecordRepository;
        private final MlResultRepository mlResultRepository;
        private final EarlyWarningRepository earlyWarningRepository;

        /**
         * Get a paginated list of all active patients, optionally filtered by search.
         *
         * If a search term is provided, matches against firstName, lastName, or MRN
         * (case-insensitive partial match).
         *
         * @param search   Optional search query string (null or blank for no filter)
         * @param pageable Pagination and sorting parameters
         * @return Page of PatientDto.Response objects
         */
        public Page<PatientDto.Response> getAllPatients(String search, Pageable pageable) {
                Page<Patient> patients = (search != null && !search.isBlank())
                                ? patientRepository.searchPatients(search, pageable)
                                : patientRepository.findAllActive(pageable);

                // Convert each Patient entity to a Response DTO
                return patients.map(this::toResponse);
        }

        /**
         * Get detailed information for a single patient, including checkup history and
         * warnings.
         *
         * Assembles a comprehensive profile by:
         * 1. Loading the patient entity from the database.
         * 2. Loading all checkup records (ordered by date descending).
         * 3. For each checkup, looking up the ML analysis result (health label, risk
         * score).
         * 4. Loading all active (unacknowledged) warnings.
         * 5. Extracting the latest risk score from the most recent analyzed checkup.
         *
         * @param id UUID of the patient
         * @return DetailResponse DTO with full patient profile
         * @throws RuntimeException If the patient is not found
         */
        public PatientDto.DetailResponse getPatientDetail(UUID id) {
                // Step 1: Load patient
                Patient patient = patientRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));

                // Step 2: Load all checkup records for this patient
                List<CheckupRecord> records = checkupRecordRepository
                                .findByPatientIdOrderByRecordDateDesc(id);

                // Step 3: Build checkup summary list with ML results
                List<PatientDto.CheckupSummary> checkupHistory = records.stream()
                                .map(r -> {
                                        // Look up ML analysis results for this checkup record
                                        List<MlAnalysisResult> results = mlResultRepository.findByRecordId(r.getId());
                                        MlAnalysisResult latest = results.isEmpty() ? null : results.get(0);
                                        return PatientDto.CheckupSummary.builder()
                                                        .recordId(r.getId())
                                                        .recordDate(r.getRecordDate())
                                                        .status(r.getStatus())
                                                        .healthLabel(latest != null ? latest.getHealthLabel() : null)
                                                        .riskScore(latest != null ? latest.getRiskScore() : null)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Step 4: Load active (unacknowledged) warnings for this patient
                List<EarlyWarning> warnings = earlyWarningRepository
                                .findByPatientIdAndAcknowledgedFalseOrderByTriggeredAtDesc(id);

                // Convert warning entities to response DTOs
                List<PatientDto.WarningResponse> warningResponses = warnings.stream()
                                .map(w -> PatientDto.WarningResponse.builder()
                                                .id(w.getId())
                                                .warningType(w.getWarningType())
                                                .severity(w.getSeverity())
                                                .message(w.getMessage())
                                                .recommendation(w.getRecommendation())
                                                .triggeredAt(w.getTriggeredAt())
                                                .acknowledged(w.getAcknowledged())
                                                .build())
                                .collect(Collectors.toList());

                // Step 5: Get the latest risk score from the most recent checkup with ML
                // results
                var latestRisk = checkupHistory.stream()
                                .filter(c -> c.getRiskScore() != null)
                                .findFirst();

                // Build and return the full detail response
                return PatientDto.DetailResponse.builder()
                                .id(patient.getId())
                                .mrn(patient.getMrn())
                                .firstName(patient.getFirstName())
                                .lastName(patient.getLastName())
                                .age(patient.getAge())
                                .sex(patient.getSex())
                                .dateOfBirth(patient.getDateOfBirth())
                                .latestRiskLabel(latestRisk.map(PatientDto.CheckupSummary::getHealthLabel).orElse(null))
                                .latestRiskScore(latestRisk.map(PatientDto.CheckupSummary::getRiskScore).orElse(null))
                                .checkupHistory(checkupHistory)
                                .activeWarnings(warningResponses)
                                .createdAt(patient.getCreatedAt())
                                .build();
        }

        /**
         * Create a new patient in the database.
         *
         * @param request CreateRequest DTO with patient demographics
         * @return Response DTO with the created patient's data
         */
        @Transactional
        public PatientDto.Response createPatient(PatientDto.CreateRequest request) {
                Patient patient = Patient.builder()
                                .mrn(request.getMrn())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .age(request.getAge())
                                .sex(request.getSex())
                                .dateOfBirth(request.getDateOfBirth())
                                .build();
                patient = patientRepository.save(patient);
                log.info("Patient created: {} {}", patient.getFirstName(), patient.getLastName());
                return toResponse(patient);
        }

        /**
         * Update an existing patient's demographics.
         * Note: MRN is not updated (it's the unique hospital identifier).
         *
         * @param id      UUID of the patient to update
         * @param request CreateRequest DTO with new values
         * @return Response DTO with updated patient data
         * @throws RuntimeException If the patient is not found
         */
        @Transactional
        public PatientDto.Response updatePatient(UUID id, PatientDto.CreateRequest request) {
                Patient patient = patientRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));
                patient.setFirstName(request.getFirstName());
                patient.setLastName(request.getLastName());
                patient.setAge(request.getAge());
                patient.setSex(request.getSex());
                patient.setDateOfBirth(request.getDateOfBirth());
                patient = patientRepository.save(patient);
                return toResponse(patient);
        }

        /**
         * Soft-delete a patient by setting isDeleted=true.
         * The patient's data remains in the database for audit/historical purposes.
         *
         * @param id UUID of the patient to delete
         * @throws RuntimeException If the patient is not found
         */
        @Transactional
        public void deletePatient(UUID id) {
                Patient patient = patientRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));
                patient.setIsDeleted(true);
                patientRepository.save(patient);
                log.info("Patient soft-deleted: {}", id);
        }

        /**
         * Convert a Patient entity to a basic Response DTO (without checkup history).
         * Used for list views.
         *
         * @param patient The Patient entity
         * @return PatientDto.Response with basic patient info
         */
        private PatientDto.Response toResponse(Patient patient) {
                return PatientDto.Response.builder()
                                .id(patient.getId())
                                .mrn(patient.getMrn())
                                .firstName(patient.getFirstName())
                                .lastName(patient.getLastName())
                                .age(patient.getAge())
                                .sex(patient.getSex())
                                .dateOfBirth(patient.getDateOfBirth())
                                .createdAt(patient.getCreatedAt())
                                .build();
        }
}
