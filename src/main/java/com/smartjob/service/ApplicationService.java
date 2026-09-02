package com.smartjob.service;

import com.smartjob.model.Application;
import com.smartjob.model.ApplicationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing job applications, status transitions, and duplicate prevention.
 */
public class ApplicationService {

    // Fast O(1) in-memory storage indexed by Application ID
    private final Map<String, Application> applicationMap;

    public ApplicationService() {
        this.applicationMap = new LinkedHashMap<>();
    }

    public ApplicationService(List<Application> initialApplications) {
        this.applicationMap = new LinkedHashMap<>();
        if (initialApplications != null) {
            for (Application app : initialApplications) {
                if (app != null && app.getId() != null) {
                    this.applicationMap.put(app.getId(), app);
                }
            }
        }
    }

    /**
     * Submits a new job application.
     * Prevents duplicate applications for the same candidate and job pair.
     *
     * @param candidateId ID of the applying candidate
     * @param jobId ID of the job
     * @return Newly created Application
     * @throws IllegalStateException if the candidate has already applied to this job
     * @throws IllegalArgumentException if candidateId or jobId is invalid
     */
    public Application apply(String candidateId, String jobId) {
        if (candidateId == null || candidateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate ID cannot be empty.");
        }
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be empty.");
        }

        String candIdClean = candidateId.trim();
        String jobIdClean = jobId.trim();

        if (hasAlreadyApplied(candIdClean, jobIdClean)) {
            throw new IllegalStateException("You have already applied for this job.");
        }

        String nextId = generateNextApplicationId();
        Application application = new Application(
                nextId,
                candIdClean,
                jobIdClean,
                LocalDate.now().toString(),
                ApplicationStatus.APPLIED
        );

        applicationMap.put(nextId, application);
        return application;
    }

    /**
     * Checks if a candidate has already applied to a given job.
     */
    public boolean hasAlreadyApplied(String candidateId, String jobId) {
        if (candidateId == null || jobId == null) {
            return false;
        }
        for (Application app : applicationMap.values()) {
            if (app.getCandidateId().equalsIgnoreCase(candidateId.trim()) &&
                app.getJobId().equalsIgnoreCase(jobId.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves application by ID.
     */
    public Application getApplicationById(String id) {
        if (id == null) return null;
        return applicationMap.get(id.trim());
    }

    /**
     * Retrieves all applications submitted by a specific candidate.
     */
    public List<Application> getApplicationsByCandidate(String candidateId) {
        List<Application> results = new ArrayList<>();
        if (candidateId == null) return results;

        for (Application app : applicationMap.values()) {
            if (app.getCandidateId().equalsIgnoreCase(candidateId.trim())) {
                results.add(app);
            }
        }
        return results;
    }

    /**
     * Retrieves all applications received for a specific job.
     */
    public List<Application> getApplicationsByJob(String jobId) {
        List<Application> results = new ArrayList<>();
        if (jobId == null) return results;

        for (Application app : applicationMap.values()) {
            if (app.getJobId().equalsIgnoreCase(jobId.trim())) {
                results.add(app);
            }
        }
        return results;
    }

    /**
     * Returns all applications across the system.
     */
    public List<Application> getAllApplications() {
        return new ArrayList<>(applicationMap.values());
    }

    /**
     * Updates recruitment status for an application.
     */
    public boolean updateStatus(String applicationId, ApplicationStatus newStatus) {
        Application app = getApplicationById(applicationId);
        if (app == null || newStatus == null) {
            return false;
        }
        app.setStatus(newStatus);
        return true;
    }

    /**
     * Generates sequential Application ID (e.g., A001, A002, ...).
     */
    public synchronized String generateNextApplicationId() {
        int maxId = 0;
        for (String id : applicationMap.keySet()) {
            if (id.toUpperCase().startsWith("A")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("A%03d", maxId + 1);
    }

    public int getApplicationCount() {
        return applicationMap.size();
    }
}
