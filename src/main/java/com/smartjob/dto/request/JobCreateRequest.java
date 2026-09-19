package com.smartjob.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for creating a job posting.
 *
 * V1 → V2: V1 Job had requiredSkills/preferredSkills as Set<String>.
 * V2 accepts structured JobSkillRequest objects with required flag and weight.
 */
public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotBlank(message = "Company name is required")
    private String company;

    private String location;

    @PositiveOrZero(message = "Experience required must be 0 or positive")
    private double experienceRequired;

    @PositiveOrZero(message = "Minimum salary must be 0 or positive")
    private double salaryMin;

    @PositiveOrZero(message = "Maximum salary must be 0 or positive")
    private double salaryMax;

    private String jobType; // FULL_TIME, PART_TIME, CONTRACT, REMOTE, INTERNSHIP

    private String description;

    /**
     * Skills associated with this job.
     * Each skill specifies name, whether it's required, and its weight.
     */
    private List<JobSkillRequest> skills;

    public JobCreateRequest() {}

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
    public List<JobSkillRequest> getSkills() { return skills; }
    public void setSkills(List<JobSkillRequest> skills) { this.skills = skills; }

    /**
     * Nested DTO for individual skill association with a job.
     */
    public static class JobSkillRequest {

        @NotBlank(message = "Skill name is required")
        private String name;

        private boolean required = true;

        private int weight = 0; // 0 = use default (5 for required, 2 for preferred)

        public JobSkillRequest() {}

        public JobSkillRequest(String name, boolean required, int weight) {
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
