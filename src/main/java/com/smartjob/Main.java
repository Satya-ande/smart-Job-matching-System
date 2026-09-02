package com.smartjob;

import com.smartjob.model.Application;
import com.smartjob.model.ApplicationStatus;
import com.smartjob.model.Candidate;
import com.smartjob.model.Job;
import com.smartjob.model.MatchResult;
import com.smartjob.repository.FileRepository;
import com.smartjob.service.ApplicationService;
import com.smartjob.service.CandidateService;
import com.smartjob.service.JobMatcher;
import com.smartjob.service.JobService;
import com.smartjob.util.ConsoleUtils;
import com.smartjob.util.InputValidator;
import com.smartjob.util.MatchingUtils;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Main application class providing the interactive console interface,
 * workflow management, and service orchestration.
 */
public class Main {

    private final FileRepository fileRepository;
    private final CandidateService candidateService;
    private final JobService jobService;
    private final JobMatcher jobMatcher;
    private final ApplicationService applicationService;
    private final Scanner scanner;

    private String activeCandidateId = null;

    public Main() {
        this.fileRepository = new FileRepository("data");
        this.candidateService = new CandidateService(fileRepository.loadCandidates());
        this.jobService = new JobService(fileRepository.loadJobs());
        this.applicationService = new ApplicationService(fileRepository.loadApplications());
        this.jobMatcher = new JobMatcher();
        this.scanner = new Scanner(System.in);

        // Auto-select first candidate if available
        List<Candidate> candidates = candidateService.getAllCandidates();
        if (!candidates.isEmpty()) {
            this.activeCandidateId = candidates.get(0).getId();
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * Main application loop.
     */
    public void run() {
        ConsoleUtils.printHeader("SMARTJOB — Smart Job Matching & Skill Gap System");
        System.out.println("  Loaded " + candidateService.getCandidateCount() + " candidates, " +
                jobService.getJobCount() + " jobs, and " +
                applicationService.getApplicationCount() + " applications.");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = ConsoleUtils.readInt(scanner, "Enter your choice", 1, 8);
            try {
                switch (choice) {
                    case 1 -> handleCandidateMenu();
                    case 2 -> handleJobMenu();
                    case 3 -> handleSearchAndFilterMenu();
                    case 4 -> handleMatchingMenu();
                    case 5 -> handleSkillGapAnalysis();
                    case 6 -> handleApplyForJob();
                    case 7 -> handleApplicationHistoryMenu();
                    case 8 -> {
                        saveData();
                        ConsoleUtils.printSuccess("All data saved successfully. Thank you for using SmartJob!");
                        running = false;
                    }
                }
            } catch (Exception e) {
                ConsoleUtils.printError("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void displayMainMenu() {
        Candidate active = getActiveCandidate();
        String activeName = active != null ? active.getName() + " (" + active.getId() + ")" : "None Selected";

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                                  MAIN MENU                                     ");
        System.out.println("  [Active Candidate: " + activeName + "]");
        System.out.println("================================================================================");
        System.out.println("  1. Candidate Profile & Skills");
        System.out.println("  2. Job Management");
        System.out.println("  3. Search & Filter Jobs");
        System.out.println("  4. Find Matching Jobs (Weighted DSA Scoring & PriorityQueue)");
        System.out.println("  5. Skill Gap Analysis & Learning Roadmap");
        System.out.println("  6. Apply for a Job");
        System.out.println("  7. My Applications & Tracking");
        System.out.println("  8. Save & Exit");
        System.out.println("================================================================================");
    }

    // =========================================================================
    // 1. CANDIDATE MANAGEMENT SUBMENU
    // =========================================================================

    private void handleCandidateMenu() {
        boolean inSubMenu = true;
        while (inSubMenu) {
            ConsoleUtils.printSubHeader("Candidate Profile & Skill Management");
            System.out.println("  1. Create New Candidate Profile");
            System.out.println("  2. View Active Profile");
            System.out.println("  3. Switch / Select Active Profile");
            System.out.println("  4. Update Profile Details");
            System.out.println("  5. Add Skill");
            System.out.println("  6. Remove Skill");
            System.out.println("  7. View Skills");
            System.out.println("  8. List All Candidates");
            System.out.println("  9. Back to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "Choose candidate option", 1, 9);
            switch (choice) {
                case 1 -> createCandidateProfile();
                case 2 -> viewActiveCandidateProfile();
                case 3 -> switchActiveCandidate();
                case 4 -> updateCandidateProfile();
                case 5 -> addSkillToCandidate();
                case 6 -> removeSkillFromCandidate();
                case 7 -> viewCandidateSkills();
                case 8 -> listAllCandidates();
                case 9 -> inSubMenu = false;
            }
        }
    }

    private void createCandidateProfile() {
        ConsoleUtils.printSubHeader("Create Candidate Profile");
        String name = ConsoleUtils.readNonEmptyString(scanner, "Enter Full Name");
        String email;
        while (true) {
            email = ConsoleUtils.readNonEmptyString(scanner, "Enter Email Address");
            if (InputValidator.isValidEmail(email)) {
                break;
            }
            ConsoleUtils.printError("Invalid email format (e.g. user@example.com). Please re-enter.");
        }

        String education = ConsoleUtils.readNonEmptyString(scanner, "Enter Education (e.g., B.Tech CSE)");
        double experience = ConsoleUtils.readDouble(scanner, "Enter Experience in years (e.g. 0 for Fresher, 1.5)", 0.0, 50.0);
        String preferredRole = ConsoleUtils.readNonEmptyString(scanner, "Enter Preferred Role (e.g., Java Developer)");
        String preferredLocation = ConsoleUtils.readNonEmptyString(scanner, "Enter Preferred Location (e.g., Hyderabad, Remote)");
        Set<String> skills = ConsoleUtils.readSkillSet(scanner, "Enter Initial Skills");

        Candidate candidate = new Candidate(null, name, email, education, experience, preferredRole, preferredLocation, skills);
        candidateService.addCandidate(candidate);
        this.activeCandidateId = candidate.getId();
        saveData();

        ConsoleUtils.printSuccess("Candidate profile created successfully with ID: " + candidate.getId());
        ConsoleUtils.printCandidateProfile(candidate);
    }

    private void viewActiveCandidateProfile() {
        Candidate active = getActiveCandidate();
        if (active == null) {
            ConsoleUtils.printWarning("No candidate profile selected. Please create or select one.");
            return;
        }
        ConsoleUtils.printCandidateProfile(active);
    }

    private void switchActiveCandidate() {
        listAllCandidates();
        if (candidateService.getCandidateCount() == 0) return;

        String id = ConsoleUtils.readNonEmptyString(scanner, "Enter Candidate ID to select as active");
        Candidate candidate = candidateService.getCandidateById(id);
        if (candidate != null) {
            this.activeCandidateId = candidate.getId();
            ConsoleUtils.printSuccess("Switched active candidate to: " + candidate.getName() + " (" + candidate.getId() + ")");
        } else {
            ConsoleUtils.printError("Candidate ID '" + id + "' not found.");
        }
    }

    private void updateCandidateProfile() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please select or create a candidate profile first.");
            return;
        }
        ConsoleUtils.printSubHeader("Update Profile for " + candidate.getName() + " (" + candidate.getId() + ")");
        System.out.println("  (Press Enter to keep current value)");

        String name = ConsoleUtils.readOptionalString(scanner, "Name", candidate.getName());
        String email = ConsoleUtils.readOptionalString(scanner, "Email", candidate.getEmail());
        if (!InputValidator.isValidEmail(email)) {
            ConsoleUtils.printWarning("Invalid email format provided. Keeping original email.");
            email = candidate.getEmail();
        }
        String edu = ConsoleUtils.readOptionalString(scanner, "Education", candidate.getEducation());

        System.out.print("Experience in years [" + candidate.getExperience() + "]: ");
        String expInput = scanner.nextLine();
        double exp = candidate.getExperience();
        if (expInput != null && !expInput.trim().isEmpty()) {
            try {
                double val = Double.parseDouble(expInput.trim());
                if (InputValidator.isValidExperience(val)) {
                    exp = val;
                }
            } catch (NumberFormatException ignored) {
                ConsoleUtils.printWarning("Invalid experience format. Retaining previous value.");
            }
        }

        String role = ConsoleUtils.readOptionalString(scanner, "Preferred Role", candidate.getPreferredRole());
        String loc = ConsoleUtils.readOptionalString(scanner, "Preferred Location", candidate.getPreferredLocation());

        candidate.setName(name);
        candidate.setEmail(email);
        candidate.setEducation(edu);
        candidate.setExperience(exp);
        candidate.setPreferredRole(role);
        candidate.setPreferredLocation(loc);

        candidateService.updateCandidate(candidate);
        saveData();
        ConsoleUtils.printSuccess("Candidate profile updated successfully.");
        ConsoleUtils.printCandidateProfile(candidate);
    }

    private void addSkillToCandidate() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please select or create a candidate profile first.");
            return;
        }
        String skill = ConsoleUtils.readNonEmptyString(scanner, "Enter Skill to Add");
        boolean added = candidate.addSkill(skill);
        if (added) {
            saveData();
            ConsoleUtils.printSuccess("Skill '" + skill + "' added successfully to " + candidate.getName() + "'s profile.");
        } else {
            ConsoleUtils.printWarning("Skill '" + skill + "' already exists in the profile.");
        }
    }

    private void removeSkillFromCandidate() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please select or create a candidate profile first.");
            return;
        }
        if (candidate.getSkills().isEmpty()) {
            ConsoleUtils.printWarning("Candidate has no skills to remove.");
            return;
        }
        viewCandidateSkills();
        String skill = ConsoleUtils.readNonEmptyString(scanner, "Enter Skill to Remove");
        boolean removed = candidate.removeSkill(skill);
        if (removed) {
            saveData();
            ConsoleUtils.printSuccess("Skill '" + skill + "' removed successfully.");
        } else {
            ConsoleUtils.printError("Skill '" + skill + "' was not found in profile.");
        }
    }

    private void viewCandidateSkills() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please select or create a candidate profile first.");
            return;
        }
        System.out.println();
        System.out.println("  Current Skills for " + candidate.getName() + " (" + candidate.getId() + "):");
        Set<String> skills = candidate.getSkills();
        if (skills.isEmpty()) {
            System.out.println("    (No skills added yet)");
        } else {
            int i = 1;
            for (String s : skills) {
                System.out.printf("    %2d. %s%n", i++, s);
            }
        }
    }

    private void listAllCandidates() {
        List<Candidate> list = candidateService.getAllCandidates();
        if (list.isEmpty()) {
            ConsoleUtils.printWarning("No candidates registered in the system.");
            return;
        }
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-20s | %-25s | %-11s | %-22s | %-15s |%n",
                "ID", "Name", "Email", "Experience", "Preferred Role", "Location");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        for (Candidate c : list) {
            String activeFlag = c.getId().equalsIgnoreCase(activeCandidateId) ? " *" : "";
            System.out.printf("| %-5s | %-20.20s | %-25.25s | %-11.11s | %-22.22s | %-15.15s |%n",
                    c.getId() + activeFlag, c.getName(), c.getEmail(),
                    MatchingUtils.formatExperience(c.getExperience()), c.getPreferredRole(), c.getPreferredLocation());
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.println("(* indicates currently active profile)");
    }

    // =========================================================================
    // 2. JOB MANAGEMENT SUBMENU
    // =========================================================================

    private void handleJobMenu() {
        boolean inSubMenu = true;
        while (inSubMenu) {
            ConsoleUtils.printSubHeader("Job Management");
            System.out.println("  1. Add New Job");
            System.out.println("  2. View All Jobs");
            System.out.println("  3. View Job Details");
            System.out.println("  4. Update Job");
            System.out.println("  5. Delete Job");
            System.out.println("  6. Back to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "Choose job option", 1, 6);
            switch (choice) {
                case 1 -> addJob();
                case 2 -> viewAllJobs();
                case 3 -> viewJobDetails();
                case 4 -> updateJob();
                case 5 -> deleteJob();
                case 6 -> inSubMenu = false;
            }
        }
    }

    private void addJob() {
        ConsoleUtils.printSubHeader("Add New Job Posting");
        String title = ConsoleUtils.readNonEmptyString(scanner, "Job Title (e.g. Java Backend Developer)");
        String company = ConsoleUtils.readNonEmptyString(scanner, "Company Name");
        String location = ConsoleUtils.readNonEmptyString(scanner, "Location (e.g. Hyderabad, Remote)");
        double expReq = ConsoleUtils.readDouble(scanner, "Experience Required in years (e.g. 0, 2.0)", 0.0, 40.0);
        double salMin = ConsoleUtils.readDouble(scanner, "Minimum Salary in INR (e.g. 600000)", 0.0, 100000000.0);
        double salMax = ConsoleUtils.readDouble(scanner, "Maximum Salary in INR (e.g. 900000)", salMin, 100000000.0);

        Set<String> reqSkills = ConsoleUtils.readSkillSet(scanner, "Required Skills (5 pts weight)");
        Set<String> prefSkills = ConsoleUtils.readSkillSet(scanner, "Preferred Skills (2 pts weight)");
        String jobType = ConsoleUtils.readOptionalString(scanner, "Job Type (e.g. Full Time, Part Time, Contract)", "Full Time");
        String description = ConsoleUtils.readNonEmptyString(scanner, "Job Description");

        Job job = new Job(null, title, company, location, expReq, salMin, salMax, reqSkills, prefSkills, jobType, description);
        jobService.addJob(job);
        saveData();

        ConsoleUtils.printSuccess("Job created successfully with ID: " + job.getId());
        ConsoleUtils.printJobDetails(job);
    }

    private void viewAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        ConsoleUtils.printJobsTable(jobs);
    }

    private void viewJobDetails() {
        viewAllJobs();
        if (jobService.getJobCount() == 0) return;

        String id = ConsoleUtils.readNonEmptyString(scanner, "Enter Job ID to view details");
        Job job = jobService.getJobById(id);
        if (job != null) {
            ConsoleUtils.printJobDetails(job);
        } else {
            ConsoleUtils.printError("Job ID '" + id + "' not found.");
        }
    }

    private void updateJob() {
        viewAllJobs();
        if (jobService.getJobCount() == 0) return;

        String id = ConsoleUtils.readNonEmptyString(scanner, "Enter Job ID to update");
        Job job = jobService.getJobById(id);
        if (job == null) {
            ConsoleUtils.printError("Job ID '" + id + "' not found.");
            return;
        }

        ConsoleUtils.printSubHeader("Updating Job " + job.getId());
        System.out.println("  (Press Enter to keep existing value)");

        String title = ConsoleUtils.readOptionalString(scanner, "Title", job.getTitle());
        String company = ConsoleUtils.readOptionalString(scanner, "Company", job.getCompany());
        String location = ConsoleUtils.readOptionalString(scanner, "Location", job.getLocation());

        System.out.print("Experience Required [" + job.getExperienceRequired() + "]: ");
        String expInput = scanner.nextLine();
        double expReq = job.getExperienceRequired();
        if (expInput != null && !expInput.trim().isEmpty()) {
            try {
                expReq = Double.parseDouble(expInput.trim());
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Min Salary [" + job.getSalaryMin() + "]: ");
        String salMinInput = scanner.nextLine();
        double salMin = job.getSalaryMin();
        if (salMinInput != null && !salMinInput.trim().isEmpty()) {
            try {
                salMin = Double.parseDouble(salMinInput.trim());
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Max Salary [" + job.getSalaryMax() + "]: ");
        String salMaxInput = scanner.nextLine();
        double salMax = job.getSalaryMax();
        if (salMaxInput != null && !salMaxInput.trim().isEmpty()) {
            try {
                salMax = Double.parseDouble(salMaxInput.trim());
            } catch (NumberFormatException ignored) {}
        }

        String jobType = ConsoleUtils.readOptionalString(scanner, "Job Type", job.getJobType());
        String description = ConsoleUtils.readOptionalString(scanner, "Description", job.getDescription());

        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setExperienceRequired(expReq);
        job.setSalaryMin(salMin);
        job.setSalaryMax(salMax);
        job.setJobType(jobType);
        job.setDescription(description);

        jobService.updateJob(job);
        saveData();
        ConsoleUtils.printSuccess("Job " + job.getId() + " updated successfully.");
        ConsoleUtils.printJobDetails(job);
    }

    private void deleteJob() {
        viewAllJobs();
        if (jobService.getJobCount() == 0) return;

        String id = ConsoleUtils.readNonEmptyString(scanner, "Enter Job ID to delete");
        boolean deleted = jobService.deleteJob(id);
        if (deleted) {
            saveData();
            ConsoleUtils.printSuccess("Job " + id + " has been deleted.");
        } else {
            ConsoleUtils.printError("Job ID '" + id + "' not found.");
        }
    }

    // =========================================================================
    // 3. SEARCH & FILTER SUBMENU
    // =========================================================================

    private void handleSearchAndFilterMenu() {
        boolean inSubMenu = true;
        while (inSubMenu) {
            ConsoleUtils.printSubHeader("Search & Filter Jobs");
            System.out.println("  1. Search by Job Title (Partial match)");
            System.out.println("  2. Search by Skill (Required or Preferred)");
            System.out.println("  3. Search by Company Name");
            System.out.println("  4. Search by Location");
            System.out.println("  5. Combinatorial Filter (Location + Salary + Experience + Type)");
            System.out.println("  6. Back to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "Choose search option", 1, 6);
            switch (choice) {
                case 1 -> {
                    String query = ConsoleUtils.readNonEmptyString(scanner, "Enter job title keyword (e.g. Java)");
                    List<Job> results = jobService.searchByTitle(query);
                    ConsoleUtils.printJobsTable(results);
                }
                case 2 -> {
                    String skill = ConsoleUtils.readNonEmptyString(scanner, "Enter skill keyword (e.g. SQL, Spring Boot)");
                    List<Job> results = jobService.searchBySkill(skill);
                    ConsoleUtils.printJobsTable(results);
                }
                case 3 -> {
                    String comp = ConsoleUtils.readNonEmptyString(scanner, "Enter company keyword (e.g. ABC, Tech)");
                    List<Job> results = jobService.searchByCompany(comp);
                    ConsoleUtils.printJobsTable(results);
                }
                case 4 -> {
                    String loc = ConsoleUtils.readNonEmptyString(scanner, "Enter location keyword (e.g. Hyderabad, Remote)");
                    List<Job> results = jobService.searchByLocation(loc);
                    ConsoleUtils.printJobsTable(results);
                }
                case 5 -> handleCombinatorialFilter();
                case 6 -> inSubMenu = false;
            }
        }
    }

    private void handleCombinatorialFilter() {
        ConsoleUtils.printSubHeader("Apply Multi-Criteria Filters (Leave blank to skip)");

        System.out.print("Location filter (e.g. Hyderabad, Pune) [Skip]: ");
        String loc = scanner.nextLine();
        if (loc != null && loc.trim().isEmpty()) loc = null;

        System.out.print("Minimum Salary in LPA (e.g. 6.0) [Skip]: ");
        String minSalStr = scanner.nextLine();
        Double minSal = null;
        if (minSalStr != null && !minSalStr.trim().isEmpty()) {
            try {
                minSal = Double.parseDouble(minSalStr.trim()) * 100000.0;
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Maximum Required Experience in years (e.g. 2.0) [Skip]: ");
        String maxExpStr = scanner.nextLine();
        Double maxExp = null;
        if (maxExpStr != null && !maxExpStr.trim().isEmpty()) {
            try {
                maxExp = Double.parseDouble(maxExpStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Job Type (e.g. Full Time, Remote) [Skip]: ");
        String jType = scanner.nextLine();
        if (jType != null && jType.trim().isEmpty()) jType = null;

        List<Job> filtered = jobService.filterJobs(loc, minSal, maxExp, jType);
        ConsoleUtils.printJobsTable(filtered);
    }

    // =========================================================================
    // 4. SMART MATCHING & PRIORITYQUEUE RANKING SUBMENU
    // =========================================================================

    private void handleMatchingMenu() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please create or select an active candidate profile first.");
            return;
        }

        boolean inSubMenu = true;
        while (inSubMenu) {
            ConsoleUtils.printSubHeader("Smart Job Matching for " + candidate.getName() + " (" + candidate.getId() + ")");
            System.out.println("  1. Match All Available Jobs (Full Ranking)");
            System.out.println("  2. Top 5 Best Matching Jobs (Ranked via PriorityQueue)");
            System.out.println("  3. Match with a Specific Job ID");
            System.out.println("  4. Back to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "Choose matching option", 1, 4);
            switch (choice) {
                case 1 -> {
                    List<MatchResult> results = jobMatcher.matchAll(candidate, jobService.getAllJobs());
                    displayMatchRankings("ALL MATCHING JOBS (Ranked by Match Score)", results);
                }
                case 2 -> {
                    List<MatchResult> topResults = jobMatcher.getTopMatchingJobs(candidate, jobService.getAllJobs(), 5);
                    displayMatchRankings("TOP 5 MATCHING JOBS (PriorityQueue Max-Heap Evaluation)", topResults);
                }
                case 3 -> {
                    viewAllJobs();
                    String jobId = ConsoleUtils.readNonEmptyString(scanner, "Enter Job ID to match against");
                    Job job = jobService.getJobById(jobId);
                    if (job != null) {
                        MatchResult res = jobMatcher.match(candidate, job);
                        ConsoleUtils.printMatchResult(res);
                    } else {
                        ConsoleUtils.printError("Job ID '" + jobId + "' not found.");
                    }
                }
                case 4 -> inSubMenu = false;
            }
        }
    }

    private void displayMatchRankings(String title, List<MatchResult> results) {
        if (results == null || results.isEmpty()) {
            ConsoleUtils.printWarning("No jobs found to evaluate.");
            return;
        }
        ConsoleUtils.printSubHeader(title);
        int rank = 1;
        for (MatchResult res : results) {
            Job j = res.getJob();
            System.out.println();
            System.out.printf("  #%d. %s at %s%n", rank++, j.getTitle(), j.getCompany());
            System.out.printf("      Job ID: %-5s | Location: %-12s | Exp Req: %s%n",
                    j.getId(), j.getLocation(), MatchingUtils.formatExperience(j.getExperienceRequired()));
            System.out.printf("      Salary: %s%n", MatchingUtils.formatSalary(j.getSalaryMin(), j.getSalaryMax()));
            System.out.printf("      COMPOSITE MATCH SCORE : %.2f%%%n", res.getMatchScore());
            System.out.printf("        * Skill Score       : %.2f%% (%d/%d pts earned)%n",
                    res.getSkillScore(), res.getEarnedSkillPoints(), res.getTotalSkillPoints());
            System.out.printf("        * Matching Skills   : %s%n",
                    res.getMatchingRequiredSkills().isEmpty() && res.getMatchingPreferredSkills().isEmpty() ? "None" :
                            String.join(", ", res.getMatchingRequiredSkills()) + (res.getMatchingPreferredSkills().isEmpty() ? "" : " | (Pref) " + String.join(", ", res.getMatchingPreferredSkills())));
            System.out.printf("        * Missing Req Skills: %s%n",
                    res.getMissingRequiredSkills().isEmpty() ? "None" : String.join(", ", res.getMissingRequiredSkills()));
        }
        System.out.println(ConsoleUtils.DIVIDER_SINGLE);
    }

    // =========================================================================
    // 5. SKILL GAP ANALYSIS & LEARNING ROADMAP
    // =========================================================================

    private void handleSkillGapAnalysis() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please select or create an active candidate profile first.");
            return;
        }

        viewAllJobs();
        if (jobService.getJobCount() == 0) return;

        String jobId = ConsoleUtils.readNonEmptyString(scanner, "Enter Job ID for dedicated Skill Gap Analysis");
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            ConsoleUtils.printError("Job ID '" + jobId + "' not found.");
            return;
        }

        MatchResult res = jobMatcher.match(candidate, job);
        ConsoleUtils.printSkillGapAnalysis(res);
    }

    // =========================================================================
    // 6. APPLY FOR JOB
    // =========================================================================

    private void handleApplyForJob() {
        Candidate candidate = getActiveCandidate();
        if (candidate == null) {
            ConsoleUtils.printWarning("Please select or create an active candidate profile first.");
            return;
        }

        viewAllJobs();
        if (jobService.getJobCount() == 0) return;

        String jobId = ConsoleUtils.readNonEmptyString(scanner, "Enter Job ID to submit application for");
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            ConsoleUtils.printError("Job ID '" + jobId + "' not found.");
            return;
        }

        try {
            Application app = applicationService.apply(candidate.getId(), job.getId());
            saveData();
            ConsoleUtils.printSuccess("Application submitted successfully!");
            System.out.printf("  Application ID: %s | Candidate: %s | Job: %s (%s) | Status: %s%n",
                    app.getId(), candidate.getName(), job.getTitle(), job.getCompany(), app.getStatus().getDisplayName());
        } catch (IllegalStateException e) {
            ConsoleUtils.printWarning(e.getMessage());
        } catch (Exception e) {
            ConsoleUtils.printError("Application failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. MY APPLICATIONS & TRACKING SUBMENU
    // =========================================================================

    private void handleApplicationHistoryMenu() {
        boolean inSubMenu = true;
        while (inSubMenu) {
            ConsoleUtils.printSubHeader("Job Applications & Status Tracking");
            System.out.println("  1. View My Applications (Active Candidate)");
            System.out.println("  2. View All System Applications");
            System.out.println("  3. Update Application Status (Recruiter / Admin Action)");
            System.out.println("  4. Back to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "Choose application option", 1, 4);
            switch (choice) {
                case 1 -> {
                    Candidate candidate = getActiveCandidate();
                    if (candidate == null) {
                        ConsoleUtils.printWarning("Please select or create an active candidate profile first.");
                        return;
                    }
                    List<Application> myApps = applicationService.getApplicationsByCandidate(candidate.getId());
                    ConsoleUtils.printApplicationsTable(myApps, buildCandidateLookupMap(), buildJobLookupMap());
                }
                case 2 -> {
                    List<Application> allApps = applicationService.getAllApplications();
                    ConsoleUtils.printApplicationsTable(allApps, buildCandidateLookupMap(), buildJobLookupMap());
                }
                case 3 -> handleUpdateApplicationStatus();
                case 4 -> inSubMenu = false;
            }
        }
    }

    private void handleUpdateApplicationStatus() {
        List<Application> allApps = applicationService.getAllApplications();
        ConsoleUtils.printApplicationsTable(allApps, buildCandidateLookupMap(), buildJobLookupMap());
        if (allApps.isEmpty()) return;

        String appId = ConsoleUtils.readNonEmptyString(scanner, "Enter Application ID to update status");
        Application app = applicationService.getApplicationById(appId);
        if (app == null) {
            ConsoleUtils.printError("Application ID '" + appId + "' not found.");
            return;
        }

        System.out.println();
        System.out.println("  Available Statuses:");
        ApplicationStatus[] statuses = ApplicationStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("    %d. %s (%s)%n", i + 1, statuses[i].name(), statuses[i].getDisplayName());
        }

        int statusChoice = ConsoleUtils.readInt(scanner, "Select new status", 1, statuses.length);
        ApplicationStatus selectedStatus = statuses[statusChoice - 1];

        applicationService.updateStatus(app.getId(), selectedStatus);
        saveData();
        ConsoleUtils.printSuccess("Application " + app.getId() + " status updated to " + selectedStatus.getDisplayName() + ".");
    }

    // =========================================================================
    // HELPER & PERSISTENCE METHODS
    // =========================================================================

    private Candidate getActiveCandidate() {
        if (activeCandidateId == null) {
            return null;
        }
        return candidateService.getCandidateById(activeCandidateId);
    }

    private void saveData() {
        fileRepository.saveCandidates(candidateService.getAllCandidates());
        fileRepository.saveJobs(jobService.getAllJobs());
        fileRepository.saveApplications(applicationService.getAllApplications());
    }

    private java.util.Map<String, Candidate> buildCandidateLookupMap() {
        java.util.Map<String, Candidate> map = new java.util.HashMap<>();
        for (Candidate c : candidateService.getAllCandidates()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    private java.util.Map<String, Job> buildJobLookupMap() {
        java.util.Map<String, Job> map = new java.util.HashMap<>();
        for (Job j : jobService.getAllJobs()) {
            map.put(j.getId(), j);
        }
        return map;
    }
}
