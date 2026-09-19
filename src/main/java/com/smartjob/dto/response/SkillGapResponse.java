package com.smartjob.dto.response;

import java.util.Set;

/**
 * DTO for skill-gap diagnostic responses.
 *
 * Distinctly categorizes matching skills, missing required skills,
 * and missing preferred skills between a candidate and a specific job.
 */
public class SkillGapResponse {

    private Long candidateId;
    private Long jobId;
    private String jobTitle;
    private String company;
    private double matchScore;
    private double skillScore;
    private Set<String> matchingSkills;
    private Set<String> missingRequiredSkills;
    private Set<String> missingPreferredSkills;

    public SkillGapResponse() {
    }

    public SkillGapResponse(Long candidateId, Long jobId, String jobTitle, String company,
                            double matchScore, double skillScore,
                            Set<String> matchingSkills,
                            Set<String> missingRequiredSkills,
                            Set<String> missingPreferredSkills) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.matchScore = matchScore;
        this.skillScore = skillScore;
        this.matchingSkills = matchingSkills;
        this.missingRequiredSkills = missingRequiredSkills;
        this.missingPreferredSkills = missingPreferredSkills;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public Set<String> getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(Set<String> matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public Set<String> getMissingRequiredSkills() {
        return missingRequiredSkills;
    }

    public void setMissingRequiredSkills(Set<String> missingRequiredSkills) {
        this.missingRequiredSkills = missingRequiredSkills;
    }

    public Set<String> getMissingPreferredSkills() {
        return missingPreferredSkills;
    }

    public void setMissingPreferredSkills(Set<String> missingPreferredSkills) {
        this.missingPreferredSkills = missingPreferredSkills;
    }
}
