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

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final CheckupRecordRepository checkupRecordRepository;
    private final MlResultRepository mlResultRepository;
    private final EarlyWarningRepository earlyWarningRepository;

    public Page<PatientDto.Response> getAllPatients(String search, Pageable pageable) {
        Page<Patient> patients = (search != null && !search.isBlank())
                ? patientRepository.searchPatients(search, pageable)
                : patientRepository.findAllActive(pageable);

        return patients.map(this::toResponse);
    }

    public PatientDto.DetailResponse getPatientDetail(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));

        List<CheckupRecord> records = checkupRecordRepository
                .findByPatientIdOrderByRecordDateDesc(id);

        List<PatientDto.CheckupSummary> checkupHistory = records.stream()
                .map(r -> {
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

        List<EarlyWarning> warnings = earlyWarningRepository
                .findByPatientIdAndAcknowledgedFalseOrderByTriggeredAtDesc(id);

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

        // Get latest risk score
        var latestRisk = checkupHistory.stream()
                .filter(c -> c.getRiskScore() != null)
                .findFirst();

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

    @Transactional
    public void deletePatient(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + id));
        patient.setIsDeleted(true);
        patientRepository.save(patient);
        log.info("Patient soft-deleted: {}", id);
    }

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
