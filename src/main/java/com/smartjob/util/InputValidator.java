package com.smartjob.util;

import java.util.regex.Pattern;

/**
 * Utility class providing validation methods for candidate profiles, jobs, and user inputs.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private InputValidator() {
        // Prevent instantiation of utility class
    }

    /**
     * Checks if a string is non-null and not empty after trimming.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validates a candidate or job name/title.
     */
    public static boolean isValidName(String name) {
        return isNotEmpty(name) && name.trim().length() >= 2;
    }

    /**
     * Validates an email address against a standard email pattern.
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates experience in years (must be non-negative and reasonable).
     */
    public static boolean isValidExperience(double experience) {
        return experience >= 0.0 && experience <= 60.0;
    }

    /**
     * Validates salary values (must be non-negative).
     */
    public static boolean isValidSalary(double salary) {
        return salary >= 0.0;
    }

    /**
     * Validates salary range where min <= max.
     */
    public static boolean isValidSalaryRange(double min, double max) {
        return min >= 0.0 && max >= min;
    }

    /**
     * Validates skill input string.
     */
    public static boolean isValidSkill(String skill) {
        return isNotEmpty(skill) && skill.trim().length() >= 1;
    }
}
