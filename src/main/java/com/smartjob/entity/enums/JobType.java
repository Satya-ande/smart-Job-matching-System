package com.smartjob.entity.enums;

/**
 * Represents job employment types.
 *
 * V1 stored jobType as a free-form String.
 * V2 normalizes it to an enum for consistency and validation.
 */
public enum JobType {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    REMOTE("Remote"),
    INTERNSHIP("Internship");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse string to JobType (case-insensitive, supports display names).
     */
    public static JobType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace(" ", "_");
        for (JobType type : JobType.values()) {
            if (type.name().equalsIgnoreCase(normalized) ||
                type.displayName.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
