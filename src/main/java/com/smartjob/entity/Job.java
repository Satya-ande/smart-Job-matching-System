package com.smartjob.entity;

import com.smartjob.entity.enums.JobType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a job opening.
 *
 * V1 → V2 Evolution:
 *   V1: Plain POJO with String ID (J001), requiredSkills/preferredSkills as Set<String>
 *   V2: JPA Entity with Long auto-ID, linked to recruiter User,
 *       skills managed via JobSkill join entity (with required flag + weight)
 *
 * Database Table: jobs
 */
@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_job_title", columnList = "title"),
    @Index(name = "idx_job_location", columnList = "location"),
    @Index(name = "idx_job_company", columnList = "company")
})
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    @Column(name = "experience_required")
    private double experienceRequired; // in years

    @Column(name = "salary_min")
    private double salaryMin;

    @Column(name = "salary_max")
    private double salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The recruiter who created this job.
     * V1 had no recruiter association — jobs existed independently.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private User recruiter;

    /**
     * Job-skill associations with required/preferred flag and weight.
     * V1 had separate Set<String> requiredSkills and Set<String> preferredSkills.
     * V2 normalizes this into a join table with metadata.
     */
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSkill> jobSkills = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Job() {
    }

    public Job(String title, String company, String location,
               double experienceRequired, double salaryMin, double salaryMax,
               JobType jobType, String description, User recruiter) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.experienceRequired = experienceRequired;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.jobType = jobType;
        this.description = description;
        this.recruiter = recruiter;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Skill Management Helpers =====

    public void addJobSkill(JobSkill jobSkill) {
        jobSkills.add(jobSkill);
        jobSkill.setJob(this);
    }

    public void removeJobSkill(JobSkill jobSkill) {
        jobSkills.remove(jobSkill);
        jobSkill.setJob(null);
    }

    public void clearJobSkills() {
        jobSkills.forEach(js -> js.setJob(null));
        jobSkills.clear();
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(double experienceRequired) {
        this.experienceRequired = Math.max(0.0, experienceRequired);
    }

    public double getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(double salaryMin) {
        this.salaryMin = Math.max(0.0, salaryMin);
    }

    public double getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(double salaryMax) {
        this.salaryMax = Math.max(0.0, salaryMax);
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(User recruiter) {
        this.recruiter = recruiter;
    }

    public List<JobSkill> getJobSkills() {
        return jobSkills;
    }

    public void setJobSkills(List<JobSkill> jobSkills) {
        this.jobSkills = jobSkills != null ? jobSkills : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
