package com.medicalml.repository;

import com.medicalml.entity.EarlyWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EarlyWarningRepository extends JpaRepository<EarlyWarning, UUID> {
    List<EarlyWarning> findByPatientIdAndAcknowledgedFalseOrderByTriggeredAtDesc(UUID patientId);
    Page<EarlyWarning> findByAcknowledgedFalseOrderByTriggeredAtDesc(Pageable pageable);
    long countByAcknowledgedFalse();
}
