package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.dto.PatientDto;
import com.medicalml.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Patient CRUD operations.
 *
 * Base path: /api/patients
 * Requires authentication. DELETE operations require ADMIN role.
 *
 * Provides endpoints for listing, searching, creating, updating,
 * and soft-deleting patient records.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    /** Service layer handling patient business logic. */
    private final PatientService patientService;

    /**
     * List all active patients with optional search and pagination.
     *
     * If a 'search' query parameter is provided, it searches by first name,
     * last name, or MRN (case-insensitive partial match).
     * Otherwise, returns all non-deleted patients.
     *
     * Pagination is handled by Spring's Pageable: pass
     * ?page=0&size=20&sort=lastName,asc
     *
     * @param search   Optional search query string
     * @param pageable Pagination/sorting parameters (auto-parsed from query params)
     * @return 200 OK with paginated list of PatientDto.Response objects
     */
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String search, Pageable pageable) {
        Page<PatientDto.Response> patients = patientService.getAllPatients(search, pageable);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patients).message("Patients retrieved").build());
    }

    /**
     * Get detailed information for a single patient by ID.
     *
     * Returns the full patient profile including:
     * - Basic demographics (name, age, sex, DOB)
     * - Checkup history with health labels and risk scores
     * - Active (unacknowledged) early warnings
     * - Latest risk score and label
     *
     * @param id UUID of the patient
     * @return 200 OK with PatientDto.DetailResponse, or RuntimeException if not
     *         found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patientService.getPatientDetail(id)).message("Patient retrieved").build());
    }

    /**
     * Create a new patient record.
     *
     * Required fields (validated by @Valid):
     * - mrn: Medical Record Number (must be unique)
     * - firstName: Patient's first name
     * - lastName: Patient's last name
     *
     * Optional fields: age, sex, dateOfBirth
     *
     * @param request CreateRequest DTO with patient data
     * @return 200 OK with created PatientDto.Response
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PatientDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patientService.createPatient(request)).message("Patient created").build());
    }

    /**
     * Update an existing patient's information.
     *
     * Updates all fields from the request body. The MRN is not changed
     * (only firstName, lastName, age, sex, dateOfBirth are updated).
     *
     * @param id      UUID of the patient to update
     * @param request CreateRequest DTO with updated data
     * @return 200 OK with updated PatientDto.Response, or RuntimeException if not
     *         found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody PatientDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patientService.updatePatient(id, request)).message("Patient updated").build());
    }

    /**
     * Soft-delete a patient (sets isDeleted=true instead of removing from
     * database).
     * Restricted to ADMIN role via @PreAuthorize.
     *
     * Soft-deleted patients are excluded from listing and search results
     * but remain in the database for audit and data integrity purposes.
     *
     * @param id UUID of the patient to delete
     * @return 200 OK with deletion confirmation, or RuntimeException if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Patient deleted").build());
    }
}
