package com.medicalml.repository;

import com.medicalml.entity.EarlyWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for EarlyWarning entities.
 *
 * Provides methods to query health alerts that need clinician attention.
 */
public interface EarlyWarningRepository extends JpaRepository<EarlyWarning, UUID> {

    /**
     * Find all unacknowledged warnings for a specific patient, newest first.
     * Used on the patient profile page to show active health alerts.
     *
     * @param patientId UUID of the patient
     * @return List of active (unacknowledged) EarlyWarning entities
     */
    List<EarlyWarning> findByPatientIdAndAcknowledgedFalseOrderByTriggeredAtDesc(UUID patientId);

    /**
     * Find all unacknowledged warnings across all patients with pagination, newest
     * first.
     * Used on the dashboard alerts panel to show system-wide active alerts.
     *
     * @param pageable Pagination parameters
     * @return Paginated list of active EarlyWarning entities
     */
    Page<EarlyWarning> findByAcknowledgedFalseOrderByTriggeredAtDesc(Pageable pageable);

    /**
     * Count total unacknowledged warnings across all patients.
     * Used for the "active alerts" counter on the dashboard.
     *
     * @return Number of unacknowledged warnings
     */
    long countByAcknowledgedFalse();
}
