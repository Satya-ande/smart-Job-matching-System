package com.smartjob.dto.response;

import java.util.Set;

/**
 * DTO for match result responses.
 *
 * Mirrors V1's MatchResult with the same score breakdown.
 * Preserves: matchScore, skillScore, experienceScore, locationScore,
 *            matching/missing required/preferred skills.
 */
public class MatchResultResponse {

    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private double salaryMin;
    private double salaryMax;

    // V1 scoring model preserved
    private double matchScore;       // Final composite score (0-100)
    private double skillScore;       // Pure skill match score (0-100)
    private double experienceScore;  // Experience suitability (0-100)
    private double locationScore;    // Location match (0-100)

    private int earnedSkillPoints;
    private int totalSkillPoints;

    private Set<String> matchingRequiredSkills;
    private Set<String> missingRequiredSkills;
    private Set<String> matchingPreferredSkills;
    private Set<String> missingPreferredSkills;

    public MatchResultResponse() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(double salaryMin) { this.salaryMin = salaryMin; }
    public double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(double salaryMax) { this.salaryMax = salaryMax; }
    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
    public double getSkillScore() { return skillScore; }
    public void setSkillScore(double skillScore) { this.skillScore = skillScore; }
    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }
    public double getLocationScore() { return locationScore; }
    public void setLocationScore(double locationScore) { this.locationScore = locationScore; }
    public int getEarnedSkillPoints() { return earnedSkillPoints; }
    public void setEarnedSkillPoints(int earnedSkillPoints) { this.earnedSkillPoints = earnedSkillPoints; }
    public int getTotalSkillPoints() { return totalSkillPoints; }
    public void setTotalSkillPoints(int totalSkillPoints) { this.totalSkillPoints = totalSkillPoints; }
    public Set<String> getMatchingRequiredSkills() { return matchingRequiredSkills; }
    public void setMatchingRequiredSkills(Set<String> matchingRequiredSkills) { this.matchingRequiredSkills = matchingRequiredSkills; }
    public Set<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(Set<String> missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }
    public Set<String> getMatchingPreferredSkills() { return matchingPreferredSkills; }
    public void setMatchingPreferredSkills(Set<String> matchingPreferredSkills) { this.matchingPreferredSkills = matchingPreferredSkills; }
    public Set<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(Set<String> missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }
}
