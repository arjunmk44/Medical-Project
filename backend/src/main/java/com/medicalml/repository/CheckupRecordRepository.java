package com.medicalml.repository;

import com.medicalml.entity.CheckupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CheckupRecord entities.
 *
 * Provides methods to query checkup records by patient or processing status.
 */
public interface CheckupRecordRepository extends JpaRepository<CheckupRecord, UUID> {

    /**
     * Find all checkup records for a patient, ordered by date (most recent first).
     * Used on the patient profile page to display checkup history.
     *
     * @param patientId UUID of the patient
     * @return List of CheckupRecords sorted by recordDate descending
     */
    List<CheckupRecord> findByPatientIdOrderByRecordDateDesc(UUID patientId);

    /**
     * Find all records with a specific processing status (e.g., "PARSED",
     * "ANALYZED").
     * Useful for finding records that still need ML analysis.
     *
     * @param status The status to filter by
     * @return List of matching CheckupRecords
     */
    List<CheckupRecord> findByStatus(String status);
}
