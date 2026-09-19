package com.smartjob.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for job posting responses.
 * Converts JobSkill entities into readable skill lists with metadata.
 */
public class JobResponse {

    private Long id;
    private String title;
    private String company;
    private String location;
    private double experienceRequired;
    private double salaryMin;
    private double salaryMax;
    private String jobType;
    private String description;
    private String recruiterName;
    private List<JobSkillResponse> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(double experienceRequired) { this.experienceRequired = experienceRequired; }
    public double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(double salaryMin) { this.salaryMin = salaryMin; }
    public double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(double salaryMax) { this.salaryMax = salaryMax; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
    public List<JobSkillResponse> getSkills() { return skills; }
    public void setSkills(List<JobSkillResponse> skills) { this.skills = skills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Nested response for job-skill associations.
     */
    public static class JobSkillResponse {
        private String name;
        private boolean required;
        private int weight;

        public JobSkillResponse() {}

        public JobSkillResponse(String name, boolean required, int weight) {
            this.name = name;
            this.required = required;
            this.weight = weight;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
    }
}
