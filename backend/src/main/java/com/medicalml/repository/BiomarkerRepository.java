package com.medicalml.repository;

import com.medicalml.entity.Biomarker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Biomarker entities.
 *
 * Extends JpaRepository which provides standard CRUD operations:
 * - save(), findById(), findAll(), deleteById(), etc.
 *
 * Custom query methods use Spring Data's method-name query derivation.
 */
public interface BiomarkerRepository extends JpaRepository<Biomarker, UUID> {

    /**
     * Find the biomarker data associated with a specific checkup record.
     * Each checkup record has exactly one biomarker row.
     *
     * @param recordId UUID of the CheckupRecord
     * @return Optional containing the Biomarker if found, empty otherwise
     */
    Optional<Biomarker> findByRecordId(UUID recordId);
}
