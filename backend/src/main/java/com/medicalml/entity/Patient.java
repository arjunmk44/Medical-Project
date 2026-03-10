package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a patient in the system.
 * Maps to the "patients" database table.
 *
 * Each patient has a unique MRN (Medical Record Number), basic demographics,
 * and supports soft-deletion (isDeleted flag instead of physical removal).
 *
 * Relationships:
 * - One patient → many CheckupRecords (linked via patientId)
 * - One patient → many EarlyWarnings (linked via patientId)
 *
 * @Entity — Marks this class as a JPA-managed entity.
 * @Table — Specifies the database table name.
 * @Builder — Lombok: enables Patient.builder().mrn("...").build() pattern.
 */
@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Medical Record Number — unique identifier within the hospital system. Max 50
     * chars.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String mrn;

    /** Patient's first name. Max 100 chars. */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /** Patient's last name. Max 100 chars. */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /** Patient's age in years. Nullable (may not be known). */
    private Integer age;

    /** Patient's sex — single character: "M" or "F". */
    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String sex;

    /** Patient's date of birth. Nullable (may not be provided). */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Soft-delete flag. Defaults to false (active).
     * When true, the patient is excluded from queries but remains in the database.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Timestamp when this record was first inserted. Set automatically, never
     * updated.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last update. Automatically updated on every save. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
