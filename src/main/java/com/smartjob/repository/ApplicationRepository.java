package com.smartjob.repository;

import com.smartjob.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Application entity.
 *
 * V1 → V2: Replaces ApplicationService's in-memory HashMap<String,Application>
 * with database-backed persistence.
 *
 * The unique constraint on (candidate_id, job_id) in the Application entity
 * provides database-level duplicate prevention (V1 only checked in Java code).
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Check if candidate has already applied to a job.
     * V1 equivalent: ApplicationService.hasAlreadyApplied()
     */
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    /**
     * Get all applications by a candidate.
     * V1 equivalent: ApplicationService.getApplicationsByCandidate()
     */
    List<Application> findByCandidateId(Long candidateId);

    /**
     * Get all applications for a job.
     * V1 equivalent: ApplicationService.getApplicationsByJob()
     */
    List<Application> findByJobId(Long jobId);
}
