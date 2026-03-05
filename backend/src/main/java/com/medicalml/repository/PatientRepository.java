package com.medicalml.repository;

import com.medicalml.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @Query("SELECT p FROM Patient p WHERE p.isDeleted = false")
    Page<Patient> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.isDeleted = false AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.mrn) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Patient> searchPatients(@Param("query") String query, Pageable pageable);

    Optional<Patient> findByMrn(String mrn);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isDeleted = false")
    long countActive();
}
