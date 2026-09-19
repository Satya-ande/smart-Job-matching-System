package com.smartjob.service;

import com.smartjob.entity.Skill;
import com.smartjob.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing normalized skills.
 * V2 Addition: V1 stored skills as raw strings — V2 normalizes them.
 */
@Service
public class SkillServiceV2 implements SkillService {

    private final SkillRepository skillRepository;

    public SkillServiceV2(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    /**
     * Find or create a skill by name.
     * If the skill already exists (case-insensitive), return existing.
     * If not, create a new Skill entity.
     */
    @Transactional
    public Skill findOrCreate(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be empty");
        }
        String trimmed = skillName.trim();
        return skillRepository.findByNameIgnoreCase(trimmed)
            .orElseGet(() -> skillRepository.save(new Skill(trimmed)));
    }

    /**
     * Find or create multiple skills from a list of names.
     */
    @Transactional
    public Set<Skill> findOrCreateAll(List<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Skill> skills = new HashSet<>();
        for (String name : skillNames) {
            if (name != null && !name.trim().isEmpty()) {
                skills.add(findOrCreate(name));
            }
        }
        return skills;
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Optional<Skill> findById(Long id) {
        return skillRepository.findById(id);
    }

    public Optional<Skill> findByName(String name) {
        return skillRepository.findByNameIgnoreCase(name);
    }
}
