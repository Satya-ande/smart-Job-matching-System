package com.smartjob.service;

import com.smartjob.model.Candidate;
import com.smartjob.model.Job;
import com.smartjob.model.MatchResult;
import com.smartjob.util.MatchingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Core Algorithmic Engine for Candidate-Job Matching, Weighted Scoring,
 * Skill Gap Analysis, and PriorityQueue Ranking.
 */
public class JobMatcher {

    // Defined skill scoring weights
    public static final int REQUIRED_SKILL_WEIGHT = 5;
    public static final int PREFERRED_SKILL_WEIGHT = 2;

    // Composite scoring weights
    public static final double WEIGHT_SKILLS = 0.70;      // 70% Skill Compatibility
    public static final double WEIGHT_EXPERIENCE = 0.20;  // 20% Experience Compatibility
    public static final double WEIGHT_LOCATION = 0.10;    // 10% Location Compatibility

    // Pre-computed skill weight map for standard evaluation
    private static final Map<String, Integer> SKILL_TYPE_WEIGHTS = new HashMap<>();

    static {
        SKILL_TYPE_WEIGHTS.put("REQUIRED", REQUIRED_SKILL_WEIGHT);
        SKILL_TYPE_WEIGHTS.put("PREFERRED", PREFERRED_SKILL_WEIGHT);
    }

    /**
     * Comparator for ranking MatchResult objects in descending order of compatibility.
     * Primary: Higher match score first.
     * Tie-breaker 1: Higher max salary first.
     * Tie-breaker 2: Better experience suitability.
     */
    public static final Comparator<MatchResult> MATCH_RESULT_COMPARATOR = (a, b) -> {
        // 1. Primary: Match score descending
        int scoreComparison = Double.compare(b.getMatchScore(), a.getMatchScore());
        if (scoreComparison != 0) {
            return scoreComparison;
        }

        // 2. Tie-breaker: Max salary descending
        int salaryComparison = Double.compare(b.getJob().getSalaryMax(), a.getJob().getSalaryMax());
        if (salaryComparison != 0) {
            return salaryComparison;
        }

        // 3. Tie-breaker: Experience score descending
        return Double.compare(b.getExperienceScore(), a.getExperienceScore());
    };

    /**
     * Evaluates and computes the detailed match result between a candidate and a job.
     *
     * @param candidate Candidate to evaluate
     * @param job Job to match against
     * @return MatchResult containing skill breakdown and weighted score
     */
    public MatchResult match(Candidate candidate, Job job) {
        if (candidate == null || job == null) {
            throw new IllegalArgumentException("Candidate and Job cannot be null");
        }

        Set<String> candidateSkills = candidate.getSkills();
        Set<String> requiredSkills = job.getRequiredSkills();
        Set<String> preferredSkills = job.getPreferredSkills();

        Set<String> matchingRequired = new LinkedHashSet<>();
        Set<String> missingRequired = new LinkedHashSet<>();
        Set<String> matchingPreferred = new LinkedHashSet<>();
        Set<String> missingPreferred = new LinkedHashSet<>();

        int totalPossiblePoints = 0;
        int earnedPoints = 0;

        // Evaluate Required Skills (Weight = 5 each)
        for (String reqSkill : requiredSkills) {
            totalPossiblePoints += REQUIRED_SKILL_WEIGHT;
            if (MatchingUtils.setContainsIgnoreCase(candidateSkills, reqSkill)) {
                matchingRequired.add(reqSkill);
                earnedPoints += REQUIRED_SKILL_WEIGHT;
            } else {
                missingRequired.add(reqSkill);
            }
        }

        // Evaluate Preferred Skills (Weight = 2 each)
        for (String prefSkill : preferredSkills) {
            totalPossiblePoints += PREFERRED_SKILL_WEIGHT;
            if (MatchingUtils.setContainsIgnoreCase(candidateSkills, prefSkill)) {
                matchingPreferred.add(prefSkill);
                earnedPoints += PREFERRED_SKILL_WEIGHT;
            } else {
                missingPreferred.add(prefSkill);
            }
        }

        // Calculate Pure Skill Score (0.0 to 100.0)
        double skillScore;
        if (totalPossiblePoints == 0) {
            skillScore = 100.0; // If no skills specified, candidate has 100% skill match
        } else {
            skillScore = ((double) earnedPoints / totalPossiblePoints) * 100.0;
        }

        // Calculate Experience Score
        double experienceScore = calculateExperienceScore(candidate.getExperience(), job.getExperienceRequired());

        // Calculate Location Score
        double locationScore = calculateLocationScore(candidate.getPreferredLocation(), job.getLocation());

        // Calculate Final Composite Score
        double compositeScore = (WEIGHT_SKILLS * skillScore) +
                                (WEIGHT_EXPERIENCE * experienceScore) +
                                (WEIGHT_LOCATION * locationScore);

        return new MatchResult(
                job,
                MatchingUtils.roundToTwoDecimals(compositeScore),
                MatchingUtils.roundToTwoDecimals(skillScore),
                MatchingUtils.roundToTwoDecimals(experienceScore),
                MatchingUtils.roundToTwoDecimals(locationScore),
                earnedPoints,
                totalPossiblePoints,
                matchingRequired,
                missingRequired,
                matchingPreferred,
                missingPreferred
        );
    }

    /**
     * Matches a candidate against all jobs and returns results sorted by rank.
     */
    public List<MatchResult> matchAll(Candidate candidate, List<Job> jobs) {
        if (candidate == null || jobs == null || jobs.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchResult> results = new ArrayList<>();
        for (Job job : jobs) {
            results.add(match(candidate, job));
        }

        results.sort(MATCH_RESULT_COMPARATOR);
        return results;
    }

    /**
     * Retrieves the Top K matching jobs for a candidate using a PriorityQueue (Max-Heap).
     *
     * @param candidate Candidate to match
     * @param jobs List of candidate jobs
     * @param topK Number of top results to retrieve
     * @return Ordered list of top K MatchResults
     */
    public List<MatchResult> getTopMatchingJobs(Candidate candidate, List<Job> jobs, int topK) {
        if (candidate == null || jobs == null || jobs.isEmpty() || topK <= 0) {
            return Collections.emptyList();
        }

        // Initialize PriorityQueue configured with our custom descending Comparator
        PriorityQueue<MatchResult> maxHeap = new PriorityQueue<>(MATCH_RESULT_COMPARATOR);

        for (Job job : jobs) {
            MatchResult result = match(candidate, job);
            maxHeap.offer(result);
        }

        List<MatchResult> topResults = new ArrayList<>();
        int count = Math.min(topK, maxHeap.size());
        for (int i = 0; i < count; i++) {
            MatchResult polled = maxHeap.poll();
            if (polled != null) {
                topResults.add(polled);
            }
        }

        return topResults;
    }

    /**
     * Computes experience suitability score.
     */
    private double calculateExperienceScore(double candidateExp, double requiredExp) {
        if (requiredExp <= 0.0) {
            return 100.0;
        }
        if (candidateExp >= requiredExp) {
            return 100.0;
        }
        // Partial credit for candidates having some fraction of required experience
        return (candidateExp / requiredExp) * 100.0;
    }

    /**
     * Computes location suitability score.
     */
    private double calculateLocationScore(String candidateLoc, String jobLoc) {
        if (candidateLoc == null || jobLoc == null) {
            return 50.0; // neutral if unassigned
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
}
