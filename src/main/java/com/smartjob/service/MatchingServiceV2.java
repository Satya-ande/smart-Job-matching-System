package com.smartjob.service;

import com.smartjob.dto.response.MatchResultResponse;
import com.smartjob.entity.Candidate;
import com.smartjob.entity.Job;
import com.smartjob.entity.JobSkill;
import com.smartjob.entity.Skill;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.CandidateRepository;
import com.smartjob.repository.JobRepository;
import com.smartjob.util.MatchingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matching Engine — V2 Spring-aware adapter wrapping V1 algorithm.
 *
 * V1 ALGORITHM PRESERVED EXACTLY:
 *   - Required skill weight = 5 pts
 *   - Preferred skill weight = 2 pts
 *   - Composite score = 70% skill + 20% experience + 10% location
 *   - PriorityQueue for top-K ranking
 *   - Case-insensitive skill matching
 *
 * V2 Changes:
 *   - Data loaded from JPA repositories instead of HashMaps
 *   - JPA entities converted to skill name sets for algorithm compatibility
 *   - Returns MatchResultResponse DTOs instead of V1 MatchResult POJOs
 *
 * The core matching logic is independent of Spring MVC, making it testable.
 */
@Service
public class MatchingServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(MatchingServiceV2.class);

    // V1 scoring constants — centralized, not scattered
    public static final int REQUIRED_SKILL_WEIGHT = 5;
    public static final int PREFERRED_SKILL_WEIGHT = 2;
    public static final double WEIGHT_SKILLS = 0.70;
    public static final double WEIGHT_EXPERIENCE = 0.20;
    public static final double WEIGHT_LOCATION = 0.10;

    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    public MatchingServiceV2(CandidateRepository candidateRepository, JobRepository jobRepository) {
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Match a candidate against all jobs and return ranked results.
     * Uses PriorityQueue for top-K ranking (V1 DSA concept preserved).
     *
     * @param candidateId candidate to match
     * @param topK number of top results (null = return all)
     * @return ranked list of match results
     */
    @Transactional(readOnly = true)
    public List<MatchResultResponse> getMatchingJobs(Long candidateId, Integer topK) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));

        List<Job> allJobs = jobRepository.findAll();
        if (allJobs.isEmpty()) {
            return Collections.emptyList();
        }

        // Extract candidate skills as lowercase set for matching
        Set<String> candidateSkills = extractSkillNames(candidate.getSkills());

        // PriorityQueue with descending score comparator (V1 pattern preserved)
        Comparator<MatchResultResponse> comparator = (a, b) -> {
            int scoreComp = Double.compare(b.getMatchScore(), a.getMatchScore());
            if (scoreComp != 0) return scoreComp;
            int salaryComp = Double.compare(b.getSalaryMax(), a.getSalaryMax());
            if (salaryComp != 0) return salaryComp;
            return Double.compare(b.getExperienceScore(), a.getExperienceScore());
        };

        PriorityQueue<MatchResultResponse> maxHeap = new PriorityQueue<>(comparator);

        for (Job job : allJobs) {
            MatchResultResponse result = matchCandidateToJob(
                candidateSkills, candidate.getExperience(), candidate.getPreferredLocation(), job
            );
            maxHeap.offer(result);
        }

        // Extract top-K results from PriorityQueue
        int count = topK != null && topK > 0 ? Math.min(topK, maxHeap.size()) : maxHeap.size();
        List<MatchResultResponse> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MatchResultResponse polled = maxHeap.poll();
            if (polled != null) {
                results.add(polled);
            }
        }

        log.debug("Matching completed for candidateId={}: {} jobs evaluated, {} results returned",
            candidateId, allJobs.size(), results.size());
        return results;
    }

    /**
     * Compute skill-gap analysis between a candidate and a specific job.
     */
    @Transactional(readOnly = true)
    public MatchResultResponse getSkillGap(Long candidateId, Long jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        Set<String> candidateSkills = extractSkillNames(candidate.getSkills());
        return matchCandidateToJob(candidateSkills, candidate.getExperience(),
            candidate.getPreferredLocation(), job);
    }

    // ==========================================
    // V1 MATCHING ALGORITHM (PRESERVED)
    // ==========================================

    /**
     * Core matching algorithm — V1 JobMatcher.match() logic preserved.
     *
     * Evaluates a candidate's skills against a job's required and preferred skills,
     * calculates weighted skill score, experience score, and location score,
     * then computes a composite match score.
     */
    public MatchResultResponse matchCandidateToJob(
            Set<String> candidateSkills, double candidateExperience,
            String candidateLocation, Job job) {

        Set<String> matchingRequired = new LinkedHashSet<>();
        Set<String> missingRequired = new LinkedHashSet<>();
        Set<String> matchingPreferred = new LinkedHashSet<>();
        Set<String> missingPreferred = new LinkedHashSet<>();

        int totalPossiblePoints = 0;
        int earnedPoints = 0;

        // Evaluate each job skill
        for (JobSkill jobSkill : job.getJobSkills()) {
            String skillName = jobSkill.getSkill().getName();
            int weight = jobSkill.getWeight();
            totalPossiblePoints += weight;

            if (setContainsIgnoreCase(candidateSkills, skillName)) {
                earnedPoints += weight;
                if (jobSkill.isRequired()) {
                    matchingRequired.add(skillName);
                } else {
                    matchingPreferred.add(skillName);
                }
            } else {
                if (jobSkill.isRequired()) {
                    missingRequired.add(skillName);
                } else {
                    missingPreferred.add(skillName);
                }
            }
        }

        // Calculate pure skill score (0-100)
        double skillScore;
        if (totalPossiblePoints == 0) {
            skillScore = 100.0; // No skills specified = 100% match (V1 behavior)
        } else {
            skillScore = ((double) earnedPoints / totalPossiblePoints) * 100.0;
        }

        // Calculate experience score (V1 algorithm)
        double experienceScore = calculateExperienceScore(candidateExperience, job.getExperienceRequired());

        // Calculate location score (V1 algorithm)
        double locationScore = calculateLocationScore(candidateLocation, job.getLocation());

        // Calculate final composite score (V1 weights: 70% skill + 20% exp + 10% loc)
        double compositeScore = (WEIGHT_SKILLS * skillScore)
                              + (WEIGHT_EXPERIENCE * experienceScore)
                              + (WEIGHT_LOCATION * locationScore);

        // Build response DTO
        MatchResultResponse response = new MatchResultResponse();
        response.setJobId(job.getId());
        response.setJobTitle(job.getTitle());
        response.setCompany(job.getCompany());
        response.setLocation(job.getLocation());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setMatchScore(MatchingUtils.roundToTwoDecimals(compositeScore));
        response.setSkillScore(MatchingUtils.roundToTwoDecimals(skillScore));
        response.setExperienceScore(MatchingUtils.roundToTwoDecimals(experienceScore));
        response.setLocationScore(MatchingUtils.roundToTwoDecimals(locationScore));
        response.setEarnedSkillPoints(earnedPoints);
        response.setTotalSkillPoints(totalPossiblePoints);
        response.setMatchingRequiredSkills(matchingRequired);
        response.setMissingRequiredSkills(missingRequired);
        response.setMatchingPreferredSkills(matchingPreferred);
        response.setMissingPreferredSkills(missingPreferred);

        return response;
    }

    /**
     * V1 experience scoring algorithm — preserved exactly.
     */
    private double calculateExperienceScore(double candidateExp, double requiredExp) {
        if (requiredExp <= 0.0) {
            return 100.0;
        }
        if (candidateExp >= requiredExp) {
            return 100.0;
        }
        return (candidateExp / requiredExp) * 100.0;
    }

    /**
     * V1 location scoring algorithm — preserved exactly.
     */
    private double calculateLocationScore(String candidateLoc, String jobLoc) {
        if (candidateLoc == null || jobLoc == null) {
            return 50.0;
        }
        String cLoc = candidateLoc.trim().toLowerCase();
        String jLoc = jobLoc.trim().toLowerCase();

        if (cLoc.equalsIgnoreCase(jLoc)) {
            return 100.0;
        }
        if (cLoc.contains("remote") || jLoc.contains("remote")) {
            return 100.0;
        }
        if (cLoc.contains(jLoc) || jLoc.contains(cLoc)) {
            return 80.0;
        }
        return 0.0;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Extract skill names from Skill entities as a Set for matching.
     */
    private Set<String> extractSkillNames(Set<Skill> skills) {
        if (skills == null) return Collections.emptySet();
        return skills.stream()
            .map(Skill::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Case-insensitive set contains — V1 MatchingUtils pattern.
     */
    private boolean setContainsIgnoreCase(Set<String> set, String target) {
        if (set == null || target == null) return false;
        String trimmed = target.trim();
        for (String item : set) {
            if (item.equalsIgnoreCase(trimmed)) return true;
        }
        return false;
    }
}
