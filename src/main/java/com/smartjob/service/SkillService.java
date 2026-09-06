package com.smartjob.service;

import com.smartjob.entity.Skill;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for skill management service.
 * Extracted to enable Mockito mocking on Java 25+
 * (Mockito cannot mock concrete classes with inline bytecode on Java 25).
 */
public interface SkillService {

    Skill findOrCreate(String skillName);

    Set<Skill> findOrCreateAll(List<String> skillNames);

    List<Skill> getAllSkills();

    Optional<Skill> findById(Long id);

    Optional<Skill> findByName(String name);
}
