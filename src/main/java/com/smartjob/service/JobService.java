package com.smartjob.service;

import com.smartjob.model.Job;
import com.smartjob.util.MatchingUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing job postings, multi-field search, and combinatorial filtering.
 */
public class JobService {

    // Fast O(1) in-memory storage indexed by Job ID
    private final Map<String, Job> jobMap;

    public JobService() {
        this.jobMap = new LinkedHashMap<>();
    }

    public JobService(List<Job> initialJobs) {
        this.jobMap = new LinkedHashMap<>();
        if (initialJobs != null) {
            for (Job job : initialJobs) {
                if (job != null && job.getId() != null) {
                    this.jobMap.put(job.getId(), job);
                }
            }
        }
    }

    /**
     * Retrieves job by ID in O(1) time.
     */
    public Job getJobById(String id) {
        if (id == null) return null;
        return jobMap.get(id.trim());
    }

    /**
     * Returns all jobs.
     */
    public List<Job> getAllJobs() {
        return new ArrayList<>(jobMap.values());
    }

    /**
     * Checks if job exists by ID.
     */
    public boolean exists(String id) {
        return id != null && jobMap.containsKey(id.trim());
    }

    /**
     * Adds a new job with auto-generated ID (e.g., J001) if not provided.
     */
    public Job addJob(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        if (job.getId() == null || job.getId().trim().isEmpty()) {
            job.setId(generateNextJobId());
        }
        jobMap.put(job.getId(), job);
        return job;
    }

    /**
     * Updates an existing job.
     */
    public boolean updateJob(Job job) {
        if (job == null || job.getId() == null) {
            return false;
        }
        if (!jobMap.containsKey(job.getId())) {
            return false;
        }
        jobMap.put(job.getId(), job);
        return true;
    }

    /**
     * Deletes a job by ID.
     */
    public boolean deleteJob(String id) {
        if (id == null) return false;
        return jobMap.remove(id.trim()) != null;
    }

    // ==========================================
    // SEARCH CAPABILITIES (Case-Insensitive)
    // ==========================================

    /**
     * Searches jobs by title substring (case-insensitive).
     */
    public List<Job> searchByTitle(String query) {
        List<Job> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllJobs();
        }
        String lower = query.trim().toLowerCase();
        for (Job job : jobMap.values()) {
            if (job.getTitle() != null && job.getTitle().toLowerCase().contains(lower)) {
                results.add(job);
            }
        }
        return results;
    }

    /**
     * Searches jobs containing the given skill in required or preferred skills.
     */
    public List<Job> searchBySkill(String skill) {
        List<Job> results = new ArrayList<>();
        if (skill == null || skill.trim().isEmpty()) {
            return getAllJobs();
        }
        for (Job job : jobMap.values()) {
            if (MatchingUtils.setContainsIgnoreCase(job.getRequiredSkills(), skill) ||
                MatchingUtils.setContainsIgnoreCase(job.getPreferredSkills(), skill)) {
                results.add(job);
            }
        }
        return results;
    }

    /**
     * Searches jobs by company name substring.
     */
    public List<Job> searchByCompany(String company) {
        List<Job> results = new ArrayList<>();
        if (company == null || company.trim().isEmpty()) {
            return getAllJobs();
        }
        String lower = company.trim().toLowerCase();
        for (Job job : jobMap.values()) {
            if (job.getCompany() != null && job.getCompany().toLowerCase().contains(lower)) {
                results.add(job);
            }
        }
        return results;
    }

    /**
     * Searches jobs by location substring.
     */
    public List<Job> searchByLocation(String location) {
        List<Job> results = new ArrayList<>();
        if (location == null || location.trim().isEmpty()) {
            return getAllJobs();
        }
        String lower = location.trim().toLowerCase();
        for (Job job : jobMap.values()) {
            if (job.getLocation() != null && job.getLocation().toLowerCase().contains(lower)) {
                results.add(job);
            }
        }
        return results;
    }

    // ==========================================
    // FILTERING (Combinatorial)
    // ==========================================

    /**
     * Filters jobs by multiple criteria simultaneously.
     *
     * @param location filter by location (null or blank to ignore)
     * @param minSalary minimum salary threshold (null or <= 0 to ignore)
     * @param maxExperience maximum required experience in years (null or < 0 to ignore)
     * @param jobType job type filter (null or blank to ignore)
     * @return filtered list of jobs
     */
    public List<Job> filterJobs(String location, Double minSalary, Double maxExperience, String jobType) {
        List<Job> results = new ArrayList<>();
        for (Job job : jobMap.values()) {
            // Location check
            if (location != null && !location.trim().isEmpty()) {
                if (job.getLocation() == null || !job.getLocation().toLowerCase().contains(location.trim().toLowerCase())) {
                    continue;
                }
            }
            // Minimum Salary check (job's maximum salary should be at least minSalary)
            if (minSalary != null && minSalary > 0) {
                if (job.getSalaryMax() < minSalary) {
                    continue;
                }
            }
            // Maximum Experience check (candidate can apply if job requires <= maxExperience)
            if (maxExperience != null && maxExperience >= 0) {
                if (job.getExperienceRequired() > maxExperience) {
                    continue;
                }
            }
            // Job Type check
            if (jobType != null && !jobType.trim().isEmpty()) {
                if (job.getJobType() == null || !job.getJobType().equalsIgnoreCase(jobType.trim())) {
                    continue;
                }
            }
            results.add(job);
        }
        return results;
    }

    /**
     * Generates sequential Job ID (e.g. J001, J002, ...).
     */
    public synchronized String generateNextJobId() {
        int maxId = 0;
        for (String id : jobMap.keySet()) {
            if (id.toUpperCase().startsWith("J")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("J%03d", maxId + 1);
    }

    public int getJobCount() {
        return jobMap.size();
    }
}
