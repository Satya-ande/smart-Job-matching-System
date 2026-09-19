package com.smartjob.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for creating a candidate profile.
 * Maps to the V1 Candidate fields without exposing internal IDs.
 */
public class CandidateCreateRequest {

    @NotBlank(message = "Education is required")
    private String education;

    @PositiveOrZero(message = "Experience must be 0 or positive")
    private double experience;

    private String preferredRole;

    private String preferredLocation;

    /**
     * Skill names to associate with the candidate.
     * V1 stored skills as Set<String> — V2 resolves names to Skill entities.
     */
    private List<String> skills;

    public CandidateCreateRequest() {}

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public double getExperience() { return experience; }
    public void setExperience(double experience) { this.experience = experience; }
    public String getPreferredRole() { return preferredRole; }
    public void setPreferredRole(String preferredRole) { this.preferredRole = preferredRole; }
    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
