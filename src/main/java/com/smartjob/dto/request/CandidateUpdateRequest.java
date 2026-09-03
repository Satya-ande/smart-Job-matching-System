package com.smartjob.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * DTO for updating candidate profile.
 * All fields are optional — only provided fields will be updated.
 */
public class CandidateUpdateRequest {

    private String education;

    @PositiveOrZero(message = "Experience must be 0 or positive")
    private Double experience;

    private String preferredRole;

    private String preferredLocation;

    private List<String> skills;

    public CandidateUpdateRequest() {}

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public Double getExperience() { return experience; }
    public void setExperience(Double experience) { this.experience = experience; }
    public String getPreferredRole() { return preferredRole; }
    public void setPreferredRole(String preferredRole) { this.preferredRole = preferredRole; }
    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
