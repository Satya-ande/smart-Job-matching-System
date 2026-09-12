package com.smartjob.controller;

import com.smartjob.dto.request.StatusUpdateRequest;
import com.smartjob.dto.response.ApplicationResponse;
import com.smartjob.service.ApplicationServiceV2;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for job application management.
 *
 * V1 → V2: V1 managed applications through console with HashMap storage.
 * V2 exposes REST endpoints with proper HTTP status codes and duplicate prevention.
 *
 * Endpoints:
 *   POST  /api/jobs/{jobId}/apply?candidateId={id}    → Apply          → 201
 *   GET   /api/candidates/{candidateId}/applications  → My apps        → 200
 *   GET   /api/jobs/{jobId}/applications              → Job's apps     → 200
 *   GET   /api/applications/{id}                      → Get by ID      → 200
 *   PATCH /api/applications/{id}/status               → Update status  → 200
 */
@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationServiceV2 applicationService;

    public ApplicationController(ApplicationServiceV2 applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * POST /api/jobs/{jobId}/apply?candidateId={candidateId}
     * Apply for a job.
     */
    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> applyForJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) Long candidateId,
            @RequestBody(required = false) com.smartjob.dto.request.ApplicationRequest request) {
        Long resolvedCandidateId = candidateId;
        if (resolvedCandidateId == null && request != null) {
            resolvedCandidateId = request.getCandidateId();
        }
        if (resolvedCandidateId == null) {
            throw new com.smartjob.exception.InvalidRequestException("candidateId is required");
        }
        ApplicationResponse response = applicationService.apply(resolvedCandidateId, jobId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/applications
     * Apply for a job using JSON request body.
     */
    @PostMapping("/applications")
    public ResponseEntity<ApplicationResponse> submitApplication(
            @Valid @RequestBody com.smartjob.dto.request.ApplicationRequest request) {
        ApplicationResponse response = applicationService.apply(request.getCandidateId(), request.getJobId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/candidates/{candidateId}/applications
     * Get all applications by a candidate.
     */
    @GetMapping("/candidates/{candidateId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCandidate(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidateId));
    }

    /**
     * GET /api/jobs/{jobId}/applications
     * Get all applications for a job (recruiter view).
     */
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJob(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    /**
     * GET /api/applications/{id}
     * Get application by ID.
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    /**
     * PATCH /api/applications/{id}/status
     * Update application status (recruiter action).
     */
    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request.getStatus()));
    }
}
