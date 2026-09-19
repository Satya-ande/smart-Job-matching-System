package com.smartjob.util;

import com.smartjob.entity.*;
import com.smartjob.entity.enums.ApplicationStatus;
import com.smartjob.entity.enums.JobType;
import com.smartjob.entity.enums.Role;
import com.smartjob.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * Utility for one-time manual migration of legacy V1 CSV data files into the V2 JPA database.
 *
 * Imports:
 *   1. data/candidates.csv  -> Users, Skills, Candidates, CandidateSkills
 *   2. data/jobs.csv        -> Recruiter, Skills, Jobs, JobSkills (Required vs Preferred)
 *   3. data/applications.csv -> Applications with Status and AppliedDate
 *
 * Idempotent: checks for existing entities to prevent duplicate records.
 */
@Component
public class CsvMigrationUtility {

    private static final Logger log = LoggerFactory.getLogger(CsvMigrationUtility.class);

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    public CsvMigrationUtility(UserRepository userRepository,
                               CandidateRepository candidateRepository,
                               SkillRepository skillRepository,
                               JobRepository jobRepository,
                               ApplicationRepository applicationRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record MigrationSummary(int skillsCreated, int candidatesImported, int jobsImported, int applicationsImported) {}

    /**
     * Executes migration looking for the default data/ folder.
     */
    @Transactional
    public MigrationSummary migrate() {
        Path defaultDataDir = Paths.get("data");
        if (!Files.exists(defaultDataDir)) {
            defaultDataDir = Paths.get("../data");
        }
        return migrate(defaultDataDir);
    }

    /**
     * Executes migration from the specified directory path.
     */
    @Transactional
    public MigrationSummary migrate(Path dataDir) {
        log.info("Starting CSV data migration from directory: {}", dataDir.toAbsolutePath());

        int initialSkillCount = (int) skillRepository.count();
        int candidatesImported = 0;
        int jobsImported = 0;
        int applicationsImported = 0;

        Map<String, Candidate> candidateCodeMap = new HashMap<>();
        Map<String, Job> jobCodeMap = new HashMap<>();

        // Ensure a default recruiter exists for jobs migrated from V1
        User defaultRecruiter = getOrCreateDefaultRecruiter();

        // 1. Migrate Candidates
        Path candidatesFile = dataDir.resolve("candidates.csv");
        if (Files.exists(candidatesFile)) {
            candidatesImported = migrateCandidates(candidatesFile, candidateCodeMap);
        } else {
            log.warn("candidates.csv not found in {}", dataDir);
        }

        // 2. Migrate Jobs
        Path jobsFile = dataDir.resolve("jobs.csv");
        if (Files.exists(jobsFile)) {
            jobsImported = migrateJobs(jobsFile, defaultRecruiter, jobCodeMap);
        } else {
            log.warn("jobs.csv not found in {}", dataDir);
        }

        // 3. Migrate Applications
        Path appsFile = dataDir.resolve("applications.csv");
        if (Files.exists(appsFile)) {
            applicationsImported = migrateApplications(appsFile, candidateCodeMap, jobCodeMap);
        } else {
            log.warn("applications.csv not found in {}", dataDir);
        }

        int finalSkillCount = (int) skillRepository.count();
        int skillsCreated = finalSkillCount - initialSkillCount;

        log.info("CSV migration completed successfully!");
        log.info("  New Skills Created   : {}", skillsCreated);
        log.info("  Candidates Imported  : {}", candidatesImported);
        log.info("  Jobs Imported        : {}", jobsImported);
        log.info("  Applications Imported: {}", applicationsImported);

        return new MigrationSummary(skillsCreated, candidatesImported, jobsImported, applicationsImported);
    }

    private int migrateCandidates(Path file, Map<String, Candidate> candidateCodeMap) {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) return 0;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 8) continue;

                String v1Id = tokens.get(0);
                String name = tokens.get(1);
                String email = tokens.get(2);
                String education = tokens.get(3);
                double experience = parseDoubleSafe(tokens.get(4), 0.0);
                String preferredRole = tokens.get(5);
                String preferredLocation = tokens.get(6);
                String skillsStr = tokens.get(7);

                // Find or create User
                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = new User(name, email, passwordEncoder.encode("Candidate@123"), Role.CANDIDATE);
                    return userRepository.save(newUser);
                });

                // Find or create Candidate profile
                Candidate candidate = candidateRepository.findByUserId(user.getId()).orElseGet(() -> {
                    Candidate newCandidate = new Candidate(user, education, experience, preferredRole, preferredLocation);
                    return candidateRepository.save(newCandidate);
                });

                // Parse and assign skills
                if (skillsStr != null && !skillsStr.trim().isEmpty()) {
                    String[] skillNames = skillsStr.split("\\|");
                    Set<Skill> skills = new HashSet<>();
                    for (String sName : skillNames) {
                        String clean = sName.trim();
                        if (!clean.isEmpty()) {
                            skills.add(findOrCreateSkill(clean));
                        }
                    }
                    candidate.getSkills().addAll(skills);
                    candidate = candidateRepository.save(candidate);
                }

                candidateCodeMap.put(v1Id, candidate);
                count++;
            }
        } catch (IOException e) {
            log.error("Failed to read candidates.csv: {}", e.getMessage(), e);
        }
        return count;
    }

    private int migrateJobs(Path file, User recruiter, Map<String, Job> jobCodeMap) {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) return 0;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 11) continue;

                String v1Id = tokens.get(0);
                String title = tokens.get(1);
                String company = tokens.get(2);
                String location = tokens.get(3);
                double expRequired = parseDoubleSafe(tokens.get(4), 0.0);
                double salaryMin = parseDoubleSafe(tokens.get(5), 0.0);
                double salaryMax = parseDoubleSafe(tokens.get(6), 0.0);
                String reqSkillsStr = tokens.get(7);
                String prefSkillsStr = tokens.get(8);
                String jobTypeStr = tokens.get(9);
                String description = tokens.get(10);

                JobType jobType = JobType.fromString(jobTypeStr);
                if (jobType == null) jobType = JobType.FULL_TIME;

                // Check if identical job already exists
                Job job = jobRepository.findByTitleAndCompany(title, company).orElse(null);
                if (job == null) {
                    job = new Job(title, company, location, expRequired, salaryMin, salaryMax,
                                  jobType, description, recruiter);

                    // Required skills
                    if (reqSkillsStr != null && !reqSkillsStr.trim().isEmpty()) {
                        for (String sName : reqSkillsStr.split("\\|")) {
                            String clean = sName.trim();
                            if (!clean.isEmpty()) {
                                Skill skill = findOrCreateSkill(clean);
                                job.addJobSkill(new JobSkill(job, skill, true));
                            }
                        }
                    }

                    // Preferred skills
                    if (prefSkillsStr != null && !prefSkillsStr.trim().isEmpty()) {
                        for (String sName : prefSkillsStr.split("\\|")) {
                            String clean = sName.trim();
                            if (!clean.isEmpty()) {
                                Skill skill = findOrCreateSkill(clean);
                                job.addJobSkill(new JobSkill(job, skill, false));
                            }
                        }
                    }

                    job = jobRepository.save(job);
                }

                jobCodeMap.put(v1Id, job);
                count++;
            }
        } catch (IOException e) {
            log.error("Failed to read jobs.csv: {}", e.getMessage(), e);
        }
        return count;
    }

    private int migrateApplications(Path file, Map<String, Candidate> candidateCodeMap, Map<String, Job> jobCodeMap) {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) return 0;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 5) continue;

                String candidateCode = tokens.get(1);
                String jobCode = tokens.get(2);
                String dateStr = tokens.get(3);
                String statusStr = tokens.get(4);

                Candidate candidate = candidateCodeMap.get(candidateCode);
                Job job = jobCodeMap.get(jobCode);

                if (candidate != null && job != null) {
                    if (!applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
                        Application app = new Application(candidate, job);
                        ApplicationStatus status = ApplicationStatus.fromString(statusStr);
                        if (status != null) {
                            app.setStatus(status);
                        }
                        if (dateStr != null && !dateStr.trim().isEmpty()) {
                            try {
                                app.setAppliedDate(LocalDate.parse(dateStr.trim()));
                            } catch (Exception ignored) {}
                        }
                        applicationRepository.save(app);
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to read applications.csv: {}", e.getMessage(), e);
        }
        return count;
    }

    private User getOrCreateDefaultRecruiter() {
        return userRepository.findByEmail("recruiter@smartjob.com").orElseGet(() -> {
            User recruiter = new User("Tech Recruiter", "recruiter@smartjob.com",
                    passwordEncoder.encode("Recruiter@123"), Role.RECRUITER);
            return userRepository.save(recruiter);
        });
    }

    private Skill findOrCreateSkill(String skillName) {
        return skillRepository.findByNameIgnoreCase(skillName.trim())
                .orElseGet(() -> skillRepository.save(new Skill(skillName.trim())));
    }

    private double parseDoubleSafe(String val, double defaultVal) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /**
     * Parses a CSV line honoring double quotes.
     */
    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(stripQuotes(sb.toString().trim()));
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(stripQuotes(sb.toString().trim()));
        return tokens;
    }

    private static String stripQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }
}
