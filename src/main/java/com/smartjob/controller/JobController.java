package com.smartjob.controller;

import com.smartjob.dto.request.JobCreateRequest;
import com.smartjob.dto.request.JobUpdateRequest;
import com.smartjob.dto.response.JobResponse;
import com.smartjob.service.JobServiceV2;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for job posting management and search.
 *
 * V1 → V2: V1 managed jobs through console menus with in-memory search.
 * V2 exposes REST endpoints with pagination, sorting, and keyword search.
 *
 * Endpoints:
 *   POST /api/jobs              → Create job        → 201
 *   GET  /api/jobs              → List all (paged)  → 200
 *   GET  /api/jobs/{id}         → Get by ID         → 200
 *   PUT  /api/jobs/{id}         → Update job        → 200
 *   DELETE /api/jobs/{id}       → Delete job        → 204
 *   GET  /api/jobs/search       → Keyword search    → 200
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobServiceV2 jobService;

    public JobController(JobServiceV2 jobService) {
        this.jobService = jobService;
    }

    /**
     * POST /api/jobs?recruiterId={recruiterId}
     * Create a new job posting.
     * NOTE: recruiterId will come from JWT in Phase 13.
     */
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestParam Long recruiterId,
            @Valid @RequestBody JobCreateRequest request) {
        JobResponse response = jobService.createJob(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/jobs?page=0&size=10&sortBy=createdAt&sortDir=desc
     * List all jobs with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate sort field against whitelist
        if (!JobServiceV2.isAllowedSortField(sortBy)) {
            sortBy = "createdAt";
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), sort);

        return ResponseEntity.ok(jobService.getAllJobs(pageable));
    }

    /**
     * GET /api/jobs/{id}
     * Get job by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    /**
     * PUT /api/jobs/{id}
     * Update an existing job.
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobUpdateRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    /**
     * DELETE /api/jobs/{id}
     * Delete a job posting.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/jobs/search?keyword=java&page=0&size=10
     * Search jobs by keyword across title, company, description, and skills.
     * V1 equivalent: Combined searchByTitle + searchByCompany + searchBySkill
     */
    @GetMapping("/search")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(jobService.searchJobs(keyword, pageable));
    }
}
