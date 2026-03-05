package com.medicalml.repository;

import com.medicalml.entity.MlAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MlResultRepository extends JpaRepository<MlAnalysisResult, UUID> {
    List<MlAnalysisResult> findByRecordId(UUID recordId);
    List<MlAnalysisResult> findByRecordIdAndModelName(UUID recordId, String modelName);
}
