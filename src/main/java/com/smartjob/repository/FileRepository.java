package com.smartjob.repository;

import com.smartjob.model.Application;
import com.smartjob.model.ApplicationStatus;
import com.smartjob.model.Candidate;
import com.smartjob.model.Job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles CSV file persistence for Candidates, Jobs, and Applications.
 * Designed with fault tolerance to handle missing files, empty files, and corrupted rows safely.
 */
public class FileRepository {

    private final String dataDirectory;
    private final String candidatesFilePath;
    private final String jobsFilePath;
    private final String applicationsFilePath;

    public FileRepository() {
        this("data");
    }

    public FileRepository(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.candidatesFilePath = dataDirectory + File.separator + "candidates.csv";
        this.jobsFilePath = dataDirectory + File.separator + "jobs.csv";
        this.applicationsFilePath = dataDirectory + File.separator + "applications.csv";
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Path dir = Paths.get(dataDirectory);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("[WARNING] Could not create data directory: " + e.getMessage());
        }
    }

    // ==========================================
    // CANDIDATE PERSISTENCE
    // ==========================================

    /**
     * Loads all candidates from candidates.csv.
     */
    public List<Candidate> loadCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        File file = new File(candidatesFilePath);
        if (!file.exists() || file.length() == 0) {
            return candidates;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine(); // skip header row
            if (header == null) return candidates;

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> tokens = parseCsvLine(line);
                    if (tokens.size() < 7) {
                        System.err.println("[WARNING] Skipping invalid candidate record at line " + lineNum + ": insufficient columns.");
                        continue;
                    }
                    String id = tokens.get(0).trim();
                    String name = tokens.get(1).trim();
                    String email = tokens.get(2).trim();
                    String education = tokens.get(3).trim();
                    double experience = Double.parseDouble(tokens.get(4).trim());
                    String preferredRole = tokens.get(5).trim();
                    String preferredLocation = tokens.get(6).trim();

                    Set<String> skills = new LinkedHashSet<>();
                    if (tokens.size() >= 8) {
                        skills = parseSkills(tokens.get(7));
                    }

                    Candidate candidate = new Candidate(id, name, email, education, experience,
                            preferredRole, preferredLocation, skills);
                    candidates.add(candidate);
                } catch (Exception e) {
                    System.err.println("[WARNING] Skipping malformed candidate row at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read candidates CSV file: " + e.getMessage());
        }
        return candidates;
    }

    /**
     * Saves all candidates to candidates.csv.
     */
    public void saveCandidates(List<Candidate> candidates) {
        ensureDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(candidatesFilePath))) {
            writer.write("id,name,email,education,experience,preferredRole,preferredLocation,skills");
            writer.newLine();

            for (Candidate c : candidates) {
                String skillsStr = String.join("|", c.getSkills());
                String line = String.format("%s,%s,%s,%s,%.1f,%s,%s,%s",
                        escapeCsv(c.getId()),
                        escapeCsv(c.getName()),
                        escapeCsv(c.getEmail()),
                        escapeCsv(c.getEducation()),
                        c.getExperience(),
                        escapeCsv(c.getPreferredRole()),
                        escapeCsv(c.getPreferredLocation()),
                        escapeCsv(skillsStr));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write candidates CSV file: " + e.getMessage());
        }
    }

    // ==========================================
    // JOB PERSISTENCE
    // ==========================================

    /**
     * Loads all jobs from jobs.csv.
     */
    public List<Job> loadJobs() {
        List<Job> jobs = new ArrayList<>();
        File file = new File(jobsFilePath);
        if (!file.exists() || file.length() == 0) {
            return jobs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine(); // skip header
            if (header == null) return jobs;

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> tokens = parseCsvLine(line);
                    if (tokens.size() < 11) {
                        System.err.println("[WARNING] Skipping invalid job record at line " + lineNum + ": expected 11 columns, got " + tokens.size());
                        continue;
                    }
                    String id = tokens.get(0).trim();
                    String title = tokens.get(1).trim();
                    String company = tokens.get(2).trim();
                    String location = tokens.get(3).trim();
                    double expReq = Double.parseDouble(tokens.get(4).trim());
                    double salMin = Double.parseDouble(tokens.get(5).trim());
                    double salMax = Double.parseDouble(tokens.get(6).trim());

                    Set<String> reqSkills = parseSkills(tokens.get(7));
                    Set<String> prefSkills = parseSkills(tokens.get(8));
                    String jobType = tokens.get(9).trim();
                    String description = tokens.get(10).trim();

                    Job job = new Job(id, title, company, location, expReq, salMin, salMax,
                            reqSkills, prefSkills, jobType, description);
                    jobs.add(job);
                } catch (Exception e) {
                    System.err.println("[WARNING] Skipping malformed job row at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read jobs CSV file: " + e.getMessage());
        }
        return jobs;
    }

    /**
     * Saves all jobs to jobs.csv.
     */
    public void saveJobs(List<Job> jobs) {
        ensureDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jobsFilePath))) {
            writer.write("id,title,company,location,experienceRequired,salaryMin,salaryMax,requiredSkills,preferredSkills,jobType,description");
            writer.newLine();

            for (Job j : jobs) {
                String reqSkillsStr = String.join("|", j.getRequiredSkills());
                String prefSkillsStr = String.join("|", j.getPreferredSkills());
                String line = String.format("%s,%s,%s,%s,%.1f,%.1f,%.1f,%s,%s,%s,%s",
                        escapeCsv(j.getId()),
                        escapeCsv(j.getTitle()),
                        escapeCsv(j.getCompany()),
                        escapeCsv(j.getLocation()),
                        j.getExperienceRequired(),
                        j.getSalaryMin(),
                        j.getSalaryMax(),
                        escapeCsv(reqSkillsStr),
                        escapeCsv(prefSkillsStr),
                        escapeCsv(j.getJobType()),
                        escapeCsv(j.getDescription()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write jobs CSV file: " + e.getMessage());
        }
    }

    // ==========================================
    // APPLICATION PERSISTENCE
    // ==========================================

    /**
     * Loads all applications from applications.csv.
     */
    public List<Application> loadApplications() {
        List<Application> applications = new ArrayList<>();
        File file = new File(applicationsFilePath);
        if (!file.exists() || file.length() == 0) {
            return applications;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine(); // skip header
            if (header == null) return applications;

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> tokens = parseCsvLine(line);
                    if (tokens.size() < 5) {
                        System.err.println("[WARNING] Skipping invalid application record at line " + lineNum);
                        continue;
                    }
                    String id = tokens.get(0).trim();
                    String candidateId = tokens.get(1).trim();
                    String jobId = tokens.get(2).trim();
                    String date = tokens.get(3).trim();
                    ApplicationStatus status = ApplicationStatus.fromString(tokens.get(4).trim());
                    if (status == null) {
                        status = ApplicationStatus.APPLIED;
                    }

                    Application app = new Application(id, candidateId, jobId, date, status);
                    applications.add(app);
                } catch (Exception e) {
                    System.err.println("[WARNING] Skipping malformed application row at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read applications CSV file: " + e.getMessage());
        }
        return applications;
    }

    /**
     * Saves all applications to applications.csv.
     */
    public void saveApplications(List<Application> applications) {
        ensureDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(applicationsFilePath))) {
            writer.write("id,candidateId,jobId,applicationDate,status");
            writer.newLine();

            for (Application a : applications) {
                String line = String.format("%s,%s,%s,%s,%s",
                        escapeCsv(a.getId()),
                        escapeCsv(a.getCandidateId()),
                        escapeCsv(a.getJobId()),
                        escapeCsv(a.getApplicationDate()),
                        a.getStatus() != null ? a.getStatus().name() : ApplicationStatus.APPLIED.name());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write applications CSV file: " + e.getMessage());
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Set<String> parseSkills(String str) {
        Set<String> skills = new LinkedHashSet<>();
        if (str == null || str.trim().isEmpty()) {
            return skills;
        }
        String clean = str.trim();
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
            clean = clean.substring(1, clean.length() - 1);
        }
        String[] tokens = clean.split("[|,]");
        for (String t : tokens) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) {
                skills.add(trimmed);
            }
        }
        return skills;
    }

    /**
     * Parses standard CSV line with quote escaping support.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    /**
     * Escapes CSV string containing commas or quotes.
     */
    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("|")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
