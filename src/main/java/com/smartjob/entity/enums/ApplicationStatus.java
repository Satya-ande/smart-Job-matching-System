package com.smartjob.entity.enums;

/**
 * Represents the recruitment lifecycle status of a job application.
 *
 * Preserved from V1 ApplicationStatus with the same values.
 * V2 Change: Moved to entity.enums package and used with @Enumerated(EnumType.STRING)
 */
public enum ApplicationStatus {
    APPLIED("Applied"),
    SHORTLISTED("Shortlisted"),
    INTERVIEW("Interview Scheduled"),
    REJECTED("Rejected"),
    SELECTED("Selected");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse string to ApplicationStatus safely (case-insensitive).
     * Preserved from V1.
     *
     * @param value the string representation
     * @return matching ApplicationStatus or null if invalid
     */
    public static ApplicationStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim()) ||
                status.displayName.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }
}
