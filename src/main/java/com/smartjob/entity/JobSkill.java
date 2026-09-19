package com.smartjob.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing the relationship between a Job and a Skill,
 * including whether the skill is required or preferred, and its weight.
 *
 * V1 → V2 Evolution:
 *   V1: Job had separate Set<String> requiredSkills (weight=5) and preferredSkills (weight=2)
 *   V2: Unified into JobSkill join entity with 'required' flag and configurable 'weight'
 *
 * This preserves the V1 weighted scoring model:
 *   - Required skill: weight = 5 (default)
 *   - Preferred skill: weight = 2 (default)
 *
 * Database Table: job_skills
 */
@Entity
@Table(name = "job_skills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "skill_id"})
})
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /**
     * true = required skill (V1 requiredSkills set)
     * false = preferred skill (V1 preferredSkills set)
     */
    @Column(nullable = false)
    private boolean required;

    /**
     * Scoring weight for the matching algorithm.
     * Default: 5 for required, 2 for preferred (matching V1 constants)
     */
    @Column(nullable = false)
    private int weight;

    public JobSkill() {
    }

    public JobSkill(Job job, Skill skill, boolean required, int weight) {
        this.job = job;
        this.skill = skill;
        this.required = required;
        this.weight = weight;
    }

    /**
     * Convenience constructor using V1 default weights.
     */
    public JobSkill(Job job, Skill skill, boolean required) {
        this.job = job;
        this.skill = skill;
        this.required = required;
        this.weight = required ? 5 : 2; // V1 constants: REQUIRED_SKILL_WEIGHT=5, PREFERRED_SKILL_WEIGHT=2
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
