package com.smartjob.controller;

import com.smartjob.dto.request.CandidateCreateRequest;
import com.smartjob.dto.request.CandidateUpdateRequest;
import com.smartjob.dto.request.SkillRequest;
import com.smartjob.dto.response.CandidateResponse;
import com.smartjob.service.CandidateServiceV2;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for candidate profile management.
 *
 * V1 → V2: V1 managed candidates through console menus.
 * V2 exposes RESTful endpoints with JSON request/response.
 *
 * Endpoints:
 *   POST   /api/candidates              → Create profile → 201
 *   GET    /api/candidates               → List all       → 200
 *   GET    /api/candidates/{id}          → Get by ID      → 200
 *   PUT    /api/candidates/{id}          → Update profile → 200
 *   DELETE /api/candidates/{id}          → Delete profile → 204
 *   POST   /api/candidates/{id}/skills   → Add skill      → 201
 *   GET    /api/candidates/{id}/skills   → List skills    → 200
 *   DELETE /api/candidates/{id}/skills/{skillId} → Remove skill → 204
 */
@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateServiceV2 candidateService;

    public CandidateController(CandidateServiceV2 candidateService) {
        this.candidateService = candidateService;
    }

    /**
     * POST /api/candidates?userId={userId}
     * Create a new candidate profile.
     * NOTE: userId will come from JWT token in Phase 13. For now, passed as query param.
     */
    @PostMapping
    public ResponseEntity<CandidateResponse> createCandidate(
            @RequestParam Long userId,
            @Valid @RequestBody CandidateCreateRequest request) {
        CandidateResponse response = candidateService.createCandidate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/candidates
     * List all candidates.
     */
    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    /**
     * GET /api/candidates/{id}
     * Get candidate by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    /**
     * PUT /api/candidates/{id}
     * Update candidate profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> updateCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidateUpdateRequest request) {
        return ResponseEntity.ok(candidateService.updateCandidate(id, request));
    }

    /**
     * DELETE /api/candidates/{id}
     * Delete candidate profile.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/candidates/{id}/skills
     * Add a skill to candidate.
     */
    @PostMapping("/{id}/skills")
    public ResponseEntity<CandidateResponse> addSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(candidateService.addSkill(id, request.getName()));
    }

    /**
     * GET /api/candidates/{id}/skills
     * List candidate's skills.
     */
    @GetMapping("/{id}/skills")
    public ResponseEntity<List<String>> getCandidateSkills(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateSkillNames(id));
    }

    /**
     * DELETE /api/candidates/{id}/skills/{skillId}
     * Remove a skill from candidate.
     */
    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> removeSkill(@PathVariable Long id, @PathVariable Long skillId) {
        candidateService.removeSkill(id, skillId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/candidates/{id}/skills
     * Replace all skills for a candidate with the provided list of skill names.
     * Used by the web UI profile editor.
     */
    @PutMapping("/{id}/skills")
    public ResponseEntity<CandidateResponse> replaceSkills(
            @PathVariable Long id,
            @RequestBody List<String> skillNames) {
        return ResponseEntity.ok(candidateService.replaceSkills(id, skillNames));
    }

    /**
     * GET /api/candidates/count
     * Returns the total number of candidates (used by stats widget).
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCandidateCount() {
        long count = candidateService.count();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
