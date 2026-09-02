package com.smartjob.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates the algorithmic evaluation between a Candidate and a Job.
 */
public class MatchResult {
    private Job job;
    private double matchScore;       // Final composite weighted match score (0.0 to 100.0)
    private double skillScore;       // Pure skill match score (0.0 to 100.0)
    private double experienceScore;  // Experience suitability score (0.0 to 100.0)
    private double locationScore;    // Location match score (0.0 to 100.0)

    private int totalSkillPoints;    // Total possible weighted points (Required*5 + Preferred*2)
    private int earnedSkillPoints;   // Total weighted points earned by candidate

    private final Set<String> matchingRequiredSkills;
    private final Set<String> missingRequiredSkills;
    private final Set<String> matchingPreferredSkills;
    private final Set<String> missingPreferredSkills;

    public MatchResult() {
        this.matchingRequiredSkills = new LinkedHashSet<>();
        this.missingRequiredSkills = new LinkedHashSet<>();
        this.matchingPreferredSkills = new LinkedHashSet<>();
        this.missingPreferredSkills = new LinkedHashSet<>();
    }

    public MatchResult(Job job, double matchScore, double skillScore,
                       double experienceScore, double locationScore,
                       int earnedSkillPoints, int totalSkillPoints,
                       Set<String> matchingRequiredSkills, Set<String> missingRequiredSkills,
                       Set<String> matchingPreferredSkills, Set<String> missingPreferredSkills) {
        this.job = job;
        this.matchScore = matchScore;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.locationScore = locationScore;
        this.earnedSkillPoints = earnedSkillPoints;
        this.totalSkillPoints = totalSkillPoints;
        this.matchingRequiredSkills = new LinkedHashSet<>(matchingRequiredSkills != null ? matchingRequiredSkills : Collections.emptySet());
        this.missingRequiredSkills = new LinkedHashSet<>(missingRequiredSkills != null ? missingRequiredSkills : Collections.emptySet());
        this.matchingPreferredSkills = new LinkedHashSet<>(matchingPreferredSkills != null ? matchingPreferredSkills : Collections.emptySet());
        this.missingPreferredSkills = new LinkedHashSet<>(missingPreferredSkills != null ? missingPreferredSkills : Collections.emptySet());
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

    public int getTotalSkillPoints() {
        return totalSkillPoints;
    }

    public void setTotalSkillPoints(int totalSkillPoints) {
        this.totalSkillPoints = totalSkillPoints;
    }

    public int getEarnedSkillPoints() {
        return earnedSkillPoints;
    }

    public void setEarnedSkillPoints(int earnedSkillPoints) {
        this.earnedSkillPoints = earnedSkillPoints;
    }

    public Set<String> getMatchingRequiredSkills() {
        return Collections.unmodifiableSet(matchingRequiredSkills);
    }

    public Set<String> getMissingRequiredSkills() {
        return Collections.unmodifiableSet(missingRequiredSkills);
    }

    public Set<String> getMatchingPreferredSkills() {
        return Collections.unmodifiableSet(matchingPreferredSkills);
    }

    public Set<String> getMissingPreferredSkills() {
        return Collections.unmodifiableSet(missingPreferredSkills);
    }

    /**
     * Returns combined list of missing skills prioritized:
     * 1. Missing Required Skills (High Priority)
     * 2. Missing Preferred Skills (Medium Priority)
     */
    public List<String> getPrioritizedSkillGaps() {
        List<String> gaps = new ArrayList<>(missingRequiredSkills);
        gaps.addAll(missingPreferredSkills);
        return gaps;
    }

    /**
     * Returns total matching skills count.
     */
    public int getTotalMatchingSkillsCount() {
        return matchingRequiredSkills.size() + matchingPreferredSkills.size();
    }

    /**
     * Returns total missing skills count.
     */
    public int getTotalMissingSkillsCount() {
        return missingRequiredSkills.size() + missingPreferredSkills.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult that)) return false;
        return Objects.equals(job, that.job);
    }

    @Override
    public int hashCode() {
        return Objects.hash(job);
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "job=" + (job != null ? job.getTitle() + " (" + job.getId() + ")" : "null") +
                ", matchScore=" + String.format("%.2f%%", matchScore) +
                ", skillScore=" + String.format("%.2f%%", skillScore) +
                ", matchingRequired=" + matchingRequiredSkills +
                ", missingRequired=" + missingRequiredSkills +
                ", matchingPreferred=" + matchingPreferredSkills +
                ", missingPreferred=" + missingPreferredSkills +
                '}';
    }
}
