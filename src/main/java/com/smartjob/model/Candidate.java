package com.smartjob.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Domain model representing a job candidate.
 */
public class Candidate {
    private String id;
    private String name;
    private String email;
    private String education;
    private double experience; // in years
    private String preferredRole;
    private String preferredLocation;
    private final Set<String> skills;

    public Candidate() {
        this.skills = new LinkedHashSet<>();
    }

    public Candidate(String id, String name, String email, String education,
                     double experience, String preferredRole, String preferredLocation) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.education = education;
        this.experience = experience;
        this.preferredRole = preferredRole;
        this.preferredLocation = preferredLocation;
        this.skills = new LinkedHashSet<>();
    }

    public Candidate(String id, String name, String email, String education,
                     double experience, String preferredRole, String preferredLocation,
                     Set<String> skills) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.education = education;
        this.experience = experience;
        this.preferredRole = preferredRole;
        this.preferredLocation = preferredLocation;
        this.skills = new LinkedHashSet<>();
        if (skills != null) {
            for (String skill : skills) {
                addSkill(skill);
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = Math.max(0.0, experience);
    }

    public String getPreferredRole() {
        return preferredRole;
    }

    public void setPreferredRole(String preferredRole) {
        this.preferredRole = preferredRole;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public Set<String> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    /**
     * Adds a skill with case-insensitive uniqueness check.
     *
     * @param skill name of the skill to add
     * @return true if added, false if already present or invalid
     */
    public boolean addSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        String trimmed = skill.trim();
        for (String existing : skills) {
            if (existing.equalsIgnoreCase(trimmed)) {
                return false; // Already present
            }
        }
        return skills.add(trimmed);
    }

    /**
     * Removes a skill with case-insensitive matching.
     *
     * @param skill name of the skill to remove
     * @return true if removed, false if not found
     */
    public boolean removeSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        String toRemove = null;
        for (String existing : skills) {
            if (existing.equalsIgnoreCase(skill.trim())) {
                toRemove = existing;
                break;
            }
        }
        if (toRemove != null) {
            return skills.remove(toRemove);
        }
        return false;
    }

    /**
     * Checks if candidate possesses a skill (case-insensitive).
     *
     * @param skill name of the skill
     * @return true if candidate has the skill
     */
    public boolean hasSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }
        for (String existing : skills) {
            if (existing.equalsIgnoreCase(skill.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets candidate skills in bulk.
     *
     * @param newSkills new skill set
     */
    public void setSkills(Set<String> newSkills) {
        this.skills.clear();
        if (newSkills != null) {
            for (String s : newSkills) {
                addSkill(s);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate candidate)) return false;
        return Objects.equals(id, candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", education='" + education + '\'' +
                ", experience=" + experience +
                ", preferredRole='" + preferredRole + '\'' +
                ", preferredLocation='" + preferredLocation + '\'' +
                ", skills=" + skills +
                '}';
    }
}
