package com.medicalml.repository;

import com.medicalml.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entities.
 *
 * Provides methods for user lookup during authentication and registration.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by username. Used during login to fetch credentials.
     *
     * @param username The username to search for
     * @return Optional containing the User if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email address. Used for account recovery and duplicate
     * checking.
     *
     * @param email The email to search for
     * @return Optional containing the User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username is already taken. Used during registration.
     *
     * @param username The username to check
     * @return true if a user with this username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email is already registered. Used during registration.
     *
     * @param email The email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);
}
