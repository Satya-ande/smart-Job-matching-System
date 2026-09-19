package com.smartjob.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for updating application status (used by recruiters).
 */
public class StatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status; // APPLIED, SHORTLISTED, INTERVIEW, REJECTED, SELECTED

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
