package com.smartjob.util;

import com.smartjob.model.Application;
import com.smartjob.model.Candidate;
import com.smartjob.model.Job;
import com.smartjob.model.MatchResult;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Utility class providing formatted console outputs, structured tables, and robust user input handlers.
 */
public final class ConsoleUtils {

    public static final String DIVIDER_DOUBLE = "================================================================================";
    public static final String DIVIDER_SINGLE = "--------------------------------------------------------------------------------";

    private ConsoleUtils() {
        // Prevent instantiation
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(DIVIDER_DOUBLE);
        int padding = (80 - title.length()) / 2;
        String formatStr = "%" + Math.max(1, padding) + "s%s";
        System.out.println(String.format(formatStr, "", title));
        System.out.println(DIVIDER_DOUBLE);
    }

    public static void printSubHeader(String title) {
        System.out.println();
        System.out.println(DIVIDER_SINGLE);
        System.out.println("  " + title);
        System.out.println(DIVIDER_SINGLE);
    }

    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("[WARNING] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * Reads a non-empty string from scanner.
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            printError("Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads an optional string. Returns defaultValue if user enters blank.
     */
    public static String readOptionalString(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String line = scanner.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return defaultValue;
        }
        return line.trim();
    }

    /**
     * Reads an integer within [min, max] range safely.
     */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " (" + min + "-" + max + "): ");
            String line = scanner.nextLine();
            try {
                int val = Integer.parseInt(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                printError("Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Reads a double value >= min safely.
     */
    public static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt + " (" + min + " - " + (max == Double.MAX_VALUE ? "any" : max) + "): ");
            String line = scanner.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                printError("Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid decimal number. Please enter a valid number (e.g., 2.5 or 500000).");
            }
        }
    }

    /**
     * Reads comma or pipe-delimited skills into a Set.
     */
    public static Set<String> readSkillSet(Scanner scanner, String prompt) {
        System.out.print(prompt + " (comma or pipe separated, e.g. Java, SQL, Git): ");
        String line = scanner.nextLine();
        Set<String> skills = new LinkedHashSet<>();
        if (line != null && !line.trim().isEmpty()) {
            String[] tokens = line.split("[,|]");
            for (String token : tokens) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    skills.add(trimmed);
                }
            }
        }
        return skills;
    }

    /**
     * Displays formatted Candidate Profile.
     */
    public static void printCandidateProfile(Candidate c) {
        if (c == null) {
            printError("Candidate record not found.");
            return;
        }
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                              CANDIDATE PROFILE                                 ");
        System.out.println("================================================================================");
        System.out.printf("  ID                 : %s%n", c.getId());
        System.out.printf("  Name               : %s%n", c.getName());
        System.out.printf("  Email              : %s%n", c.getEmail());
        System.out.printf("  Education          : %s%n", c.getEducation());
        System.out.printf("  Experience         : %s%n", MatchingUtils.formatExperience(c.getExperience()));
        System.out.printf("  Preferred Role     : %s%n", c.getPreferredRole());
        System.out.printf("  Preferred Location : %s%n", c.getPreferredLocation());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  Skills (" + c.getSkills().size() + "):");
        if (c.getSkills().isEmpty()) {
            System.out.println("    (No skills added yet)");
        } else {
            int i = 1;
            for (String s : c.getSkills()) {
                System.out.printf("    %2d. %s%n", i++, s);
            }
        }
        System.out.println("================================================================================");
    }

    /**
     * Displays detailed Job specifications.
     */
    public static void printJobDetails(Job job) {
        if (job == null) {
            printError("Job not found.");
            return;
        }
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                                 JOB DETAILS                                    ");
        System.out.println("================================================================================");
        System.out.printf("  Job ID             : %s%n", job.getId());
        System.out.printf("  Title              : %s%n", job.getTitle());
        System.out.printf("  Company            : %s%n", job.getCompany());
        System.out.printf("  Location           : %s%n", job.getLocation());
        System.out.printf("  Job Type           : %s%n", job.getJobType());
        System.out.printf("  Experience Required: %s%n", MatchingUtils.formatExperience(job.getExperienceRequired()));
        System.out.printf("  Salary Range       : %s%n", MatchingUtils.formatSalary(job.getSalaryMin(), job.getSalaryMax()));
        System.out.printf("  Description        : %s%n", job.getDescription());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  Required Skills (Weight: 5 pts each):");
        if (job.getRequiredSkills().isEmpty()) {
            System.out.println("    (None)");
        } else {
            for (String skill : job.getRequiredSkills()) {
                System.out.println("    * " + skill);
            }
        }
        System.out.println("  Preferred Skills (Weight: 2 pts each):");
        if (job.getPreferredSkills().isEmpty()) {
            System.out.println("    (None)");
        } else {
            for (String skill : job.getPreferredSkills()) {
                System.out.println("    + " + skill);
            }
        }
        System.out.println("================================================================================");
    }

    /**
     * Formats and prints a tabular list of jobs.
     */
    public static void printJobsTable(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            printWarning("No jobs found matching the criteria.");
            return;
        }
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-26s | %-20s | %-12s | %-11s | %-24s |%n",
                "ID", "Title", "Company", "Location", "Experience", "Salary Range");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        for (Job j : jobs) {
            String exp = MatchingUtils.formatExperience(j.getExperienceRequired());
            double minLpa = j.getSalaryMin() / 100000.0;
            double maxLpa = j.getSalaryMax() / 100000.0;
            String sal = String.format("%.1f - %.1f LPA", minLpa, maxLpa);
            System.out.printf("| %-5s | %-26.26s | %-20.20s | %-12.12s | %-11.11s | %-24.24s |%n",
                    j.getId(), j.getTitle(), j.getCompany(), j.getLocation(), exp, sal);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.println("Total Jobs: " + jobs.size());
    }

    /**
     * Formats and prints match results for a single candidate-job pair.
     */
    public static void printMatchResult(MatchResult res) {
        if (res == null || res.getJob() == null) {
            printError("Match result is empty.");
            return;
        }
        Job j = res.getJob();
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Job ID: %s | Title: %s%n", j.getId(), j.getTitle());
        System.out.printf("  Company: %s | Location: %s%n", j.getCompany(), j.getLocation());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  OVERALL MATCH SCORE : %6.2f%%%n", res.getMatchScore());
        System.out.printf("    - Skill Match     : %6.2f%% (%d/%d pts)%n",
                res.getSkillScore(), res.getEarnedSkillPoints(), res.getTotalSkillPoints());
        System.out.printf("    - Experience Match: %6.2f%%%n", res.getExperienceScore());
        System.out.printf("    - Location Match  : %6.2f%%%n", res.getLocationScore());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Matching Required Skills : %s%n",
                res.getMatchingRequiredSkills().isEmpty() ? "(None)" : String.join(", ", res.getMatchingRequiredSkills()));
        System.out.printf("  Missing Required Skills  : %s%n",
                res.getMissingRequiredSkills().isEmpty() ? "(None - All matched!)" : String.join(", ", res.getMissingRequiredSkills()));
        System.out.printf("  Matching Preferred Skills: %s%n",
                res.getMatchingPreferredSkills().isEmpty() ? "(None)" : String.join(", ", res.getMatchingPreferredSkills()));
        System.out.printf("  Missing Preferred Skills : %s%n",
                res.getMissingPreferredSkills().isEmpty() ? "(None)" : String.join(", ", res.getMissingPreferredSkills()));
        System.out.println("--------------------------------------------------------------------------------");
    }

    /**
     * Formats and displays dedicated Skill Gap Analysis.
     */
    public static void printSkillGapAnalysis(MatchResult res) {
        if (res == null || res.getJob() == null) {
            printError("Skill gap analysis data is not available.");
            return;
        }
        Job j = res.getJob();
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                              SKILL GAP ANALYSIS                                ");
        System.out.println("================================================================================");
        System.out.printf("  Target Role : %s%n", j.getTitle());
        System.out.printf("  Company     : %s%n", j.getCompany());
        System.out.printf("  Location    : %s%n", j.getLocation());
        System.out.printf("  Match Score : %.2f%%%n", res.getMatchScore());
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("  [MATCHED SKILLS]");
        if (res.getMatchingRequiredSkills().isEmpty() && res.getMatchingPreferredSkills().isEmpty()) {
            System.out.println("    None");
        } else {
            for (String skill : res.getMatchingRequiredSkills()) {
                System.out.println("    [MATCH] [Required]  " + skill);
            }
            for (String skill : res.getMatchingPreferredSkills()) {
                System.out.println("    [MATCH] [Preferred] " + skill);
            }
        }

        System.out.println();
        System.out.println("  [MISSING REQUIRED SKILLS] (High Impact - 5 pts each)");
        if (res.getMissingRequiredSkills().isEmpty()) {
            System.out.println("    None! You satisfy all core technical requirements.");
        } else {
            for (String skill : res.getMissingRequiredSkills()) {
                System.out.println("    [MISSING] " + skill);
            }
        }

        System.out.println();
        System.out.println("  [MISSING PREFERRED SKILLS] (Bonus Impact - 2 pts each)");
        if (res.getMissingPreferredSkills().isEmpty()) {
            System.out.println("    None! You possess all bonus / preferred skills.");
        } else {
            for (String skill : res.getMissingPreferredSkills()) {
                System.out.println("    [MISSING] " + skill);
            }
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  RECOMMENDED LEARNING ROADMAP (Priority Ordered):");
        List<String> roadmap = res.getPrioritizedSkillGaps();
        if (roadmap.isEmpty()) {
            System.out.println("    Congratulations! You are a 100% skill match for this position!");
        } else {
            int rank = 1;
            for (String gap : roadmap) {
                boolean isReq = res.getMissingRequiredSkills().contains(gap);
                String priorityLabel = isReq ? "[CRITICAL - Required]" : "[ENHANCEMENT - Preferred]";
                System.out.printf("    %2d. %-25s %s%n", rank++, gap, priorityLabel);
            }
        }
        System.out.println("================================================================================");
    }

    /**
     * Formats and prints a tabular list of job applications.
     */
    public static void printApplicationsTable(List<Application> applications,
                                              Map<String, Candidate> candidateMap,
                                              Map<String, Job> jobMap) {
        if (applications == null || applications.isEmpty()) {
            printWarning("No applications recorded.");
            return;
        }
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-10s | %-20s | %-24s | %-11s | %-20s |%n",
                "AppID", "Cand ID", "Candidate Name", "Job Title", "Date", "Status");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        for (Application app : applications) {
            Candidate cand = candidateMap.get(app.getCandidateId());
            Job job = jobMap.get(app.getJobId());
            String candName = cand != null ? cand.getName() : app.getCandidateId();
            String jobTitle = job != null ? job.getTitle() : app.getJobId();
            String status = app.getStatus() != null ? app.getStatus().getDisplayName() : "UNKNOWN";
            System.out.printf("| %-5s | %-10s | %-20.20s | %-24.24s | %-11s | %-20.20s |%n",
                    app.getId(), app.getCandidateId(), candName, jobTitle, app.getApplicationDate(), status);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.println("Total Applications: " + applications.size());
    }
}
