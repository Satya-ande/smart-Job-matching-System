package com.smartjob.controller;

import com.smartjob.dto.response.MatchResultResponse;
import com.smartjob.service.MatchingServiceV2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for job matching and skill-gap analysis.
 *
 * This is the V2 API surface for the V1 JobMatcher engine.
 * The underlying matching algorithm (weighted scoring + PriorityQueue ranking)
 * is preserved exactly from V1.
 *
 * Endpoints:
 *   GET /api/candidates/{candidateId}/matches         → Top matching jobs → 200
 *   GET /api/candidates/{candidateId}/jobs/{jobId}/skill-gap → Skill gap  → 200
 */
@RestController
@RequestMapping("/api/candidates")
public class MatchingController {

    private final MatchingServiceV2 matchingService;

    public MatchingController(MatchingServiceV2 matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * GET /api/candidates/{candidateId}/matches?topK=5
     * Get top matching jobs for a candidate.
     *
     * V1 equivalent: JobMatcher.getTopMatchingJobs(candidate, allJobs, topK)
     * Uses PriorityQueue ranking with composite score sorting.
     */
    @GetMapping("/{candidateId}/matches")
    public ResponseEntity<List<MatchResultResponse>> getMatchingJobs(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "5") Integer topK) {
        List<MatchResultResponse> results = matchingService.getMatchingJobs(candidateId, topK);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/candidates/{candidateId}/jobs/{jobId}/skill-gap
     * Get detailed skill-gap analysis between a candidate and a specific job.
     *
     * V1 equivalent: JobMatcher.match(candidate, job)
     * Returns matching/missing required/preferred skills with score breakdown.
     */
    @GetMapping("/{candidateId}/jobs/{jobId}/skill-gap")
    public ResponseEntity<MatchResultResponse> getSkillGap(
            @PathVariable Long candidateId,
            @PathVariable Long jobId) {
        MatchResultResponse result = matchingService.getSkillGap(candidateId, jobId);
        return ResponseEntity.ok(result);
    }
}
