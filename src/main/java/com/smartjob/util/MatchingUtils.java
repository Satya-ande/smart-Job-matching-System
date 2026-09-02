package com.smartjob.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

/**
 * Utility functions for skill normalization, score formatting, and mathematical rounding.
 */
public final class MatchingUtils {

    private static final Locale INDIA_LOCALE = Locale.forLanguageTag("en-IN");

    private MatchingUtils() {
        // Prevent instantiation
    }

    /**
     * Normalizes skill string by trimming extra whitespace.
     */
    public static String normalizeSkill(String skill) {
        if (skill == null) {
            return "";
        }
        return skill.trim();
    }

    /**
     * Checks if a skill set contains the given target skill (case-insensitive).
     */
    public static boolean setContainsIgnoreCase(Set<String> set, String targetSkill) {
        if (set == null || targetSkill == null) {
            return false;
        }
        String trimmedTarget = targetSkill.trim();
        for (String item : set) {
            if (item.equalsIgnoreCase(trimmedTarget)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rounds a double value to exactly 2 decimal places.
     */
    public static double roundToTwoDecimals(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Formats salary range with LPA and formatted currency amount.
     */
    public static String formatSalary(double min, double max) {
        if (min <= 0 && max <= 0) {
            return "Not Disclosed";
        }
        double minLpa = roundToTwoDecimals(min / 100000.0);
        double maxLpa = roundToTwoDecimals(max / 100000.0);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        formatter.setMaximumFractionDigits(0);

        if (min == max) {
            return String.format("%s (%.1f LPA)", formatter.format(min), minLpa);
        }
        return String.format("%s - %s (%.1f - %.1f LPA)",
                formatter.format(min), formatter.format(max), minLpa, maxLpa);
    }

    /**
     * Formats experience in years into readable string.
     */
    public static String formatExperience(double years) {
        if (years <= 0.0) {
            return "Fresher (0 years)";
        } else if (years == 1.0) {
            return "1 year";
        } else if (years == Math.floor(years)) {
            return String.format("%.0f years", years);
        } else {
            return String.format("%.1f years", years);
        }
    }
}
