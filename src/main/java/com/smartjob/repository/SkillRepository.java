package com.smartjob.repository;

import com.smartjob.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Skill entity.
 *
 * V2 Addition: V1 stored skills as raw strings.
 * V2 normalizes skills into their own table for referential integrity.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Find skill by exact name (case-insensitive).
     */
    Optional<Skill> findByNameIgnoreCase(String name);

    /**
     * Check if a skill name already exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find multiple skills by their names (case-insensitive).
     * Used when creating candidates/jobs with skill lists.
     */
    List<Skill> findByNameInIgnoreCase(List<String> names);
}
