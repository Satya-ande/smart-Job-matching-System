package com.smartjob.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for updating an existing job posting.
 */
public class JobUpdateRequest {

    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    private String company;
    private String location;

    @PositiveOrZero(message = "Experience required must be 0 or positive")
    private Double experienceRequired;

    @PositiveOrZero(message = "Minimum salary must be 0 or positive")
    private Double salaryMin;

    @PositiveOrZero(message = "Maximum salary must be 0 or positive")
    private Double salaryMax;

    private String jobType;
    private String description;
    private List<JobCreateRequest.JobSkillRequest> skills;

    public JobUpdateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(Double experienceRequired) { this.experienceRequired = experienceRequired; }
    public Double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Double salaryMin) { this.salaryMin = salaryMin; }
    public Double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Double salaryMax) { this.salaryMax = salaryMax; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<JobCreateRequest.JobSkillRequest> getSkills() { return skills; }
    public void setSkills(List<JobCreateRequest.JobSkillRequest> skills) { this.skills = skills; }
}
