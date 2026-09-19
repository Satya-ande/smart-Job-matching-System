package com.smartjob.repository;

import com.smartjob.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for Candidate entity.
 *
 * V1 → V2: Replaces CandidateService's in-memory HashMap<String,Candidate>
 * with database-backed persistence via Spring Data JPA.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    /**
     * Find candidate by associated user ID.
     */
    Optional<Candidate> findByUserId(Long userId);

    /**
     * Check if a candidate profile exists for a user.
     */
    boolean existsByUserId(Long userId);
}
