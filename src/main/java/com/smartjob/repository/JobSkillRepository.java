package com.smartjob.repository;

import com.smartjob.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for JobSkill entity (job-skill join table).
 */
@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    /**
     * Find all skill associations for a job.
     */
    List<JobSkill> findByJobId(Long jobId);

    /**
     * Delete all skill associations for a job.
     */
    void deleteByJobId(Long jobId);
}
