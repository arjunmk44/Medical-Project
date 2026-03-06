package com.medicalml.repository;

import com.medicalml.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Patient entities.
 *
 * Provides methods for querying patients with soft-delete awareness
 * (only returns patients where isDeleted=false).
 */
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Find all active (non-deleted) patients with pagination.
     * Used as the default patient list query when no search term is provided.
     *
     * @param pageable Pagination and sorting parameters
     * @return Page of active Patient entities
     */
    @Query("SELECT p FROM Patient p WHERE p.isDeleted = false")
    Page<Patient> findAllActive(Pageable pageable);

    /**
     * Search active patients by first name, last name, or MRN.
     * Case-insensitive partial matching (LIKE '%query%').
     *
     * Used when the user types in the search bar on the patient list page.
     *
     * @param query    Search string to match against name/MRN
     * @param pageable Pagination parameters
     * @return Page of matching active Patient entities
     */
    @Query("SELECT p FROM Patient p WHERE p.isDeleted = false AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.mrn) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Patient> searchPatients(@Param("query") String query, Pageable pageable);

    /**
     * Find a patient by their Medical Record Number.
     * Used during data ingestion to avoid creating duplicate patient records.
     *
     * @param mrn Medical Record Number
     * @return Optional containing the Patient if found
     */
    Optional<Patient> findByMrn(String mrn);

    /**
     * Count all active (non-deleted) patients.
     * Used for dashboard statistics.
     *
     * @return Number of active patients
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isDeleted = false")
    long countActive();
}
