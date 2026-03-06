package com.medicalml.repository;

import com.medicalml.entity.MlAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MlResultRepository extends JpaRepository<MlAnalysisResult, UUID> {
    List<MlAnalysisResult> findByRecordId(UUID recordId);

    List<MlAnalysisResult> findByRecordIdAndModelName(UUID recordId, String modelName);

    @Query("SELECT r FROM MlAnalysisResult r WHERE r.recordId IN " +
            "(SELECT c.id FROM CheckupRecord c WHERE c.patientId = :patientId) " +
            "ORDER BY r.analyzedAt DESC LIMIT 1")
    Optional<MlAnalysisResult> findLatestByPatientId(@Param("patientId") UUID patientId);
}
