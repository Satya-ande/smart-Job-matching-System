package com.smartjob.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Domain model representing a job opening.
 */
public class Job {
    private String id;
    private String title;
    private String company;
    private String location;
    private double experienceRequired; // in years
    private double salaryMin;
    private double salaryMax;
    private final Set<String> requiredSkills;
    private final Set<String> preferredSkills;
    private String jobType; // e.g. "Full Time", "Part Time", "Remote", "Contract"
    private String description;

    public Job() {
        this.requiredSkills = new LinkedHashSet<>();
        this.preferredSkills = new LinkedHashSet<>();
    }

    public Job(String id, String title, String company, String location,
               double experienceRequired, double salaryMin, double salaryMax,
               String jobType, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.experienceRequired = experienceRequired;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.jobType = jobType;
        this.description = description;
        this.requiredSkills = new LinkedHashSet<>();
        this.preferredSkills = new LinkedHashSet<>();
    }

    public Job(String id, String title, String company, String location,
               double experienceRequired, double salaryMin, double salaryMax,
               Set<String> requiredSkills, Set<String> preferredSkills,
               String jobType, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.experienceRequired = experienceRequired;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.jobType = jobType;
        this.description = description;
        this.requiredSkills = new LinkedHashSet<>();
        this.preferredSkills = new LinkedHashSet<>();
        if (requiredSkills != null) {
            for (String s : requiredSkills) {
                addRequiredSkill(s);
            }
        }
        if (preferredSkills != null) {
            for (String s : preferredSkills) {
                addPreferredSkill(s);
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(double experienceRequired) {
        this.experienceRequired = Math.max(0.0, experienceRequired);
    }

    public double getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(double salaryMin) {
        this.salaryMin = Math.max(0.0, salaryMin);
    }

    public double getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(double salaryMax) {
        this.salaryMax = Math.max(0.0, salaryMax);
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getRequiredSkills() {
        return Collections.unmodifiableSet(requiredSkills);
    }

    public Set<String> getPreferredSkills() {
        return Collections.unmodifiableSet(preferredSkills);
    }

    public boolean addRequiredSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        String trimmed = skill.trim();
        for (String s : requiredSkills) {
            if (s.equalsIgnoreCase(trimmed)) {
                return false;
            }
        }
        return requiredSkills.add(trimmed);
    }

    public boolean removeRequiredSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        String toRemove = null;
        for (String s : requiredSkills) {
            if (s.equalsIgnoreCase(skill.trim())) {
                toRemove = s;
                break;
            }
        }
        if (toRemove != null) {
            return requiredSkills.remove(toRemove);
        }
        return false;
    }

    public boolean addPreferredSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        String trimmed = skill.trim();
        for (String s : preferredSkills) {
            if (s.equalsIgnoreCase(trimmed)) {
                return false;
            }
        }
        return preferredSkills.add(trimmed);
    }

    public boolean removePreferredSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        String toRemove = null;
        for (String s : preferredSkills) {
            if (s.equalsIgnoreCase(skill.trim())) {
                toRemove = s;
                break;
            }
        }
        if (toRemove != null) {
            return preferredSkills.remove(toRemove);
        }
        return false;
    }

    public void setRequiredSkills(Set<String> skills) {
        this.requiredSkills.clear();
        if (skills != null) {
            for (String s : skills) {
                addRequiredSkill(s);
            }
        }
    }

    public void setPreferredSkills(Set<String> skills) {
        this.preferredSkills.clear();
        if (skills != null) {
            for (String s : skills) {
                addPreferredSkill(s);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job job)) return false;
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", experienceRequired=" + experienceRequired +
                ", salaryMin=" + salaryMin +
                ", salaryMax=" + salaryMax +
                ", requiredSkills=" + requiredSkills +
                ", preferredSkills=" + preferredSkills +
                ", jobType='" + jobType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
