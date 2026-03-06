package com.medicalml.repository;

import com.medicalml.entity.MlAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for MlAnalysisResult entities.
 *
 * Provides methods to query ML analysis outputs stored after pipeline
 * execution.
 */
public interface MlResultRepository extends JpaRepository<MlAnalysisResult, UUID> {

    /**
     * Find all ML analysis results for a specific checkup record.
     * A record may have multiple results if re-analyzed after model retraining.
     *
     * @param recordId UUID of the CheckupRecord
     * @return List of MlAnalysisResult entities
     */
    List<MlAnalysisResult> findByRecordId(UUID recordId);

    /**
     * Find ML results filtered by both record ID and model name.
     * Useful for retrieving results from a specific model (e.g., "ensemble").
     *
     * @param recordId  UUID of the CheckupRecord
     * @param modelName Name of the ML model
     * @return List of matching MlAnalysisResult entities
     */
    List<MlAnalysisResult> findByRecordIdAndModelName(UUID recordId, String modelName);

    /**
     * Find the most recent ML analysis result for a patient (across all their
     * checkups).
     *
     * Uses a custom JPQL query that:
     * 1. Finds all CheckupRecords belonging to the patient.
     * 2. Selects MlAnalysisResult entries linked to those records.
     * 3. Orders by analyzedAt descending and returns the first (most recent)
     * result.
     *
     * @param patientId UUID of the Patient
     * @return Optional containing the latest MlAnalysisResult, or empty if none
     *         exists
     */
    @Query("SELECT r FROM MlAnalysisResult r WHERE r.recordId IN " +
            "(SELECT c.id FROM CheckupRecord c WHERE c.patientId = :patientId) " +
            "ORDER BY r.analyzedAt DESC LIMIT 1")
    Optional<MlAnalysisResult> findLatestByPatientId(@Param("patientId") UUID patientId);
}
