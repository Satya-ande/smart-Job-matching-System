package com.smartjob.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain model representing a candidate's application for a specific job.
 */
public class Application {
    private String id;
    private String candidateId;
    private String jobId;
    private String applicationDate;
    private ApplicationStatus status;

    public Application() {
        this.applicationDate = LocalDate.now().toString();
        this.status = ApplicationStatus.APPLIED;
    }

    public Application(String id, String candidateId, String jobId) {
        this.id = id;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.applicationDate = LocalDate.now().toString();
        this.status = ApplicationStatus.APPLIED;
    }

    public Application(String id, String candidateId, String jobId, String applicationDate, ApplicationStatus status) {
        this.id = id;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.applicationDate = applicationDate != null ? applicationDate : LocalDate.now().toString();
        this.status = status != null ? status : ApplicationStatus.APPLIED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", candidateId='" + candidateId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", applicationDate='" + applicationDate + '\'' +
                ", status=" + status +
                '}';
    }
}
