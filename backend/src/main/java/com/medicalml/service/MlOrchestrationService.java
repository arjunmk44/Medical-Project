package com.medicalml.service;

import com.medicalml.entity.*;
import com.medicalml.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Orchestrates ML pipeline by calling the Python microservice.
 * Ref-1 Layers 2–6 executed via POST /ml/analyze.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MlOrchestrationService {

    private final CheckupRecordRepository recordRepository;
    private final BiomarkerRepository biomarkerRepository;
    private final MlResultRepository mlResultRepository;
    private final EarlyWarningRepository warningRepository;
    private final PatientRepository patientRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${ml.service.url:http://localhost:8001}")
    private String mlServiceUrl;

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeRecord(UUID recordId) {
        CheckupRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + recordId));

        Biomarker biomarker = biomarkerRepository.findByRecordId(recordId)
                .orElseThrow(() -> new RuntimeException("No biomarkers found for record: " + recordId));

        Patient patient = patientRepository.findById(record.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Build request payload for ML service
        Map<String, Object> mlRequest = new HashMap<>();
        mlRequest.put("patient_id", record.getPatientId().toString());
        mlRequest.put("record_id", recordId.toString());
        mlRequest.put("age", patient.getAge());
        mlRequest.put("sex", patient.getSex());

        // Add all biomarker values
        if (biomarker.getSystolicBp() != null)
            mlRequest.put("systolic_bp", biomarker.getSystolicBp().doubleValue());
        if (biomarker.getDiastolicBp() != null)
            mlRequest.put("diastolic_bp", biomarker.getDiastolicBp().doubleValue());
        if (biomarker.getHeartRate() != null)
            mlRequest.put("heart_rate", biomarker.getHeartRate().doubleValue());
        if (biomarker.getGlucose() != null)
            mlRequest.put("glucose", biomarker.getGlucose().doubleValue());
        if (biomarker.getHba1c() != null)
            mlRequest.put("hba1c", biomarker.getHba1c().doubleValue());
        if (biomarker.getTotalCholesterol() != null)
            mlRequest.put("total_cholesterol", biomarker.getTotalCholesterol().doubleValue());
        if (biomarker.getLdl() != null)
            mlRequest.put("ldl", biomarker.getLdl().doubleValue());
        if (biomarker.getHdl() != null)
            mlRequest.put("hdl", biomarker.getHdl().doubleValue());
        if (biomarker.getTriglycerides() != null)
            mlRequest.put("triglycerides", biomarker.getTriglycerides().doubleValue());
        if (biomarker.getAlt() != null)
            mlRequest.put("alt", biomarker.getAlt().doubleValue());
        if (biomarker.getAst() != null)
            mlRequest.put("ast", biomarker.getAst().doubleValue());
        if (biomarker.getAlp() != null)
            mlRequest.put("alp", biomarker.getAlp().doubleValue());
        if (biomarker.getCreatinine() != null)
            mlRequest.put("creatinine", biomarker.getCreatinine().doubleValue());
        if (biomarker.getBun() != null)
            mlRequest.put("bun", biomarker.getBun().doubleValue());
        if (biomarker.getEgfr() != null)
            mlRequest.put("egfr", biomarker.getEgfr().doubleValue());
        if (biomarker.getHeightCm() != null)
            mlRequest.put("height_cm", biomarker.getHeightCm().doubleValue());
        if (biomarker.getWeightKg() != null)
            mlRequest.put("weight_kg", biomarker.getWeightKg().doubleValue());
        if (biomarker.getBmi() != null)
            mlRequest.put("bmi", biomarker.getBmi().doubleValue());
        if (biomarker.getWaistCm() != null)
            mlRequest.put("waist_cm", biomarker.getWaistCm().doubleValue());
        if (biomarker.getHipCm() != null)
            mlRequest.put("hip_cm", biomarker.getHipCm().doubleValue());
        if (biomarker.getSmokingStatus() != null)
            mlRequest.put("smoking_status", biomarker.getSmokingStatus());
        if (biomarker.getAlcoholUnits() != null)
            mlRequest.put("alcohol_units", biomarker.getAlcoholUnits().doubleValue());
        if (biomarker.getActivityLevel() != null)
            mlRequest.put("activity_level", biomarker.getActivityLevel());
        if (biomarker.getSleepHours() != null)
            mlRequest.put("sleep_hours", biomarker.getSleepHours().doubleValue());

        log.info("Calling ML service for record: {}", recordId);

        // Call Python ML service
        Map<String, Object> mlResponse;
        try {
            mlResponse = webClientBuilder.build()
                    .post()
                    .uri(mlServiceUrl + "/ml/analyze")
                    .bodyValue(mlRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("ML service call failed", e);
            throw new RuntimeException("ML analysis failed: " + e.getMessage());
        }

        if (mlResponse == null) {
            throw new RuntimeException("ML service returned null response");
        }

        // Save ML results
        MlAnalysisResult result = MlAnalysisResult.builder()
                .recordId(recordId)
                .modelName("ensemble")
                .healthLabel((String) mlResponse.get("health_label"))
                .riskScore(new BigDecimal(mlResponse.get("risk_score").toString()))
                .clusterId((Integer) mlResponse.get("cluster_id"))
                .clusterLabel((String) mlResponse.get("cluster_label"))
                .ldaTopicDistribution(Map.of("distribution", mlResponse.get("lda_topic_distribution")))
                .pdmTopicDistribution(Map.of("distribution", mlResponse.get("pdm_topic_distribution")))
                .pcaComponents(Map.of("components", mlResponse.get("pca_components")))
                .shapValues(Map.of("values", mlResponse.get("shap_values")))
                .analyzedAt(LocalDateTime.now())
                .build();
        mlResultRepository.save(result);

        // Save warnings
        List<Map<String, Object>> warnings = (List<Map<String, Object>>) mlResponse.get("warnings");
        if (warnings != null) {
            for (Map<String, Object> w : warnings) {
                EarlyWarning warning = EarlyWarning.builder()
                        .patientId(record.getPatientId())
                        .recordId(recordId)
                        .warningType((String) w.get("type"))
                        .severity((String) w.get("severity"))
                        .message((String) w.get("message"))
                        .recommendation((String) w.get("recommendation"))
                        .build();
                warningRepository.save(warning);
            }
        }

        // Update record status
        record.setStatus("ANALYZED");
        recordRepository.save(record);

        log.info("ML analysis complete for record: {}, label: {}, score: {}",
                recordId, mlResponse.get("health_label"), mlResponse.get("risk_score"));

        return mlResponse;
    }
}
