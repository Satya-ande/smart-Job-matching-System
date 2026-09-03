package com.smartjob.repository;

import com.smartjob.entity.Job;
import com.smartjob.entity.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Job entity.
 *
 * V1 → V2: Replaces JobService's in-memory HashMap<String,Job>
 * with database-backed persistence. Adds pagination, sorting, and
 * specification-based filtering (V1 did all filtering in Java code).
 *
 * Extends JpaSpecificationExecutor for dynamic filtering in Phase 10.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    /**
     * Find jobs by recruiter user ID.
     */
    List<Job> findByRecruiterId(Long recruiterId);

    /**
     * Keyword search across title, company, and description (case-insensitive).
     * V1 equivalent: JobService.searchByTitle() + searchByCompany()
     */
    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN j.jobSkills js LEFT JOIN js.skill s " +
           "WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Job> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find jobs by location (case-insensitive partial match).
     * V1 equivalent: JobService.searchByLocation()
     */
    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    /**
     * Find jobs by title (case-insensitive partial match).
     * V1 equivalent: JobService.searchByTitle()
     */
    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find jobs by job type.
     */
    Page<Job> findByJobType(JobType jobType, Pageable pageable);
}
