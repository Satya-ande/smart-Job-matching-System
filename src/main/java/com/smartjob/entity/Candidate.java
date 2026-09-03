package com.smartjob.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity representing a job candidate.
 *
 * V1 → V2 Evolution:
 *   V1: Plain POJO with String ID (C001), skills as Set<String>
 *   V2: JPA Entity with Long auto-ID, linked to User account,
 *       skills normalized via many-to-many relationship with Skill entity
 *
 * Database Table: candidates
 */
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Each candidate is linked to a user account.
     * The user contains name, email, and authentication credentials.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String education;

    private double experience; // in years

    @Column(name = "preferred_role")
    private String preferredRole;

    @Column(name = "preferred_location")
    private String preferredLocation;

    /**
     * Many-to-many relationship with Skill.
     * V1 stored skills as Set<String> directly on the Candidate object.
     * V2 normalizes skills into a separate table for referential integrity.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "candidate_skills",
        joinColumns = @JoinColumn(name = "candidate_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Candidate() {
    }

    public Candidate(User user, String education, double experience,
                     String preferredRole, String preferredLocation) {
        this.user = user;
        this.education = education;
        this.experience = experience;
        this.preferredRole = preferredRole;
        this.preferredLocation = preferredLocation;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Skill Management (V1 logic adapted for JPA) =====

    public void addSkill(Skill skill) {
        if (skill != null) {
            this.skills.add(skill);
        }
    }

    public void removeSkill(Skill skill) {
        if (skill != null) {
            this.skills.remove(skill);
        }
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills != null ? skills : new HashSet<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
