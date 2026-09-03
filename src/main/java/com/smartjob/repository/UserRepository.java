package com.smartjob.repository;

import com.smartjob.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for User entity.
 *
 * V2 Addition: V1 had no user management.
 * Provides database-backed user lookup for authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (used for login authentication).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists (used during registration).
     */
    boolean existsByEmail(String email);
}
