package com.medicalml.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a system user (clinician, admin, etc.).
 * Maps to the "users" database table.
 *
 * Used for JWT-based authentication. Users log in with username/password
 * and receive JWT tokens to access protected API endpoints.
 *
 * Roles:
 * - "USER" — Can view patients, upload data, and run ML analysis.
 * - "ADMIN" — Can additionally delete patients, retrain models, and manage
 * users.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unique username used for login. Max 100 chars. */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** Unique email address. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never stored in plaintext. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** User role: "USER" or "ADMIN". Determines access permissions. */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * Whether the account is active. Inactive accounts cannot log in. Defaults to
     * true.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Timestamp when the user account was created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last account update. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
