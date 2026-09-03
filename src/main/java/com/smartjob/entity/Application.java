package com.smartjob.entity;

import com.smartjob.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a candidate's application for a specific job.
 *
 * V1 → V2 Evolution:
 *   V1: Plain POJO with String IDs (A001, C001, J001), stored in HashMap
 *   V2: JPA Entity with Long auto-ID, proper foreign keys to Candidate and Job,
 *       unique constraint preventing duplicate applications
 *
 * Database Table: applications
 */
@Entity
@Table(name = "applications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_candidate_job",
            columnNames = {"candidate_id", "job_id"}
        )
    },
    indexes = {
        @Index(name = "idx_application_candidate", columnList = "candidate_id"),
        @Index(name = "idx_application_job", columnList = "job_id")
    }
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The candidate who submitted the application.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * The job being applied for.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    /**
     * V1 ApplicationStatus enum preserved: APPLIED, SHORTLISTED, INTERVIEW, REJECTED, SELECTED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Application() {
        this.appliedDate = LocalDate.now();
        this.status = ApplicationStatus.APPLIED;
    }

    public Application(Candidate candidate, Job job) {
        this.candidate = candidate;
        this.job = job;
        this.appliedDate = LocalDate.now();
        this.status = ApplicationStatus.APPLIED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.appliedDate == null) {
            this.appliedDate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = ApplicationStatus.APPLIED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
