package com.smartjob.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing a normalized skill.
 *
 * V1 → V2 Evolution:
 *   V1: Skills were raw strings stored inside Candidate and Job objects
 *   V2: Skills are first-class entities in their own table,
 *       referenced by candidates (many-to-many) and jobs (via JobSkill)
 *
 * Database Table: skills
 */
@Entity
@Table(name = "skills", indexes = {
    @Index(name = "idx_skill_name", columnList = "name", unique = true)
})
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Skill() {
    }

    public Skill(String name) {
        this.name = name;
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ===== equals/hashCode based on name (case-insensitive) =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill skill)) return false;
        return name != null && name.equalsIgnoreCase(skill.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
