package com.smartjob.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for submitting a job application.
 */
public class ApplicationRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    public ApplicationRequest() {
    }

    public ApplicationRequest(Long candidateId, Long jobId) {
        this.candidateId = candidateId;
        this.jobId = jobId;
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
}
