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

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String search, Pageable pageable) {
        Page<PatientDto.Response> patients = patientService.getAllPatients(search, pageable);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patients).message("Patients retrieved").build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patientService.getPatientDetail(id)).message("Patient retrieved").build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PatientDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patientService.createPatient(request)).message("Patient created").build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody PatientDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .data(patientService.updatePatient(id, request)).message("Patient updated").build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.Success.builder()
                .message("Patient deleted").build());
    }
}
