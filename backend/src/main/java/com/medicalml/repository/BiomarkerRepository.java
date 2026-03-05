package com.medicalml.repository;

import com.medicalml.entity.Biomarker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BiomarkerRepository extends JpaRepository<Biomarker, UUID> {
    Optional<Biomarker> findByRecordId(UUID recordId);
}
