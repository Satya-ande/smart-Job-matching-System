package com.smartjob.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for candidate profile responses.
 * Exposes candidate data safely — no password, no internal entity references.
 */
public class CandidateResponse {

    private Long id;
    private String name;
    private String email;
    private String education;
    private double experience;
    private String preferredRole;
    private String preferredLocation;
    private List<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CandidateResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
