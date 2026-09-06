package com.smartjob.service;

import com.smartjob.dto.request.CandidateCreateRequest;
import com.smartjob.dto.request.CandidateUpdateRequest;
import com.smartjob.dto.response.CandidateResponse;
import com.smartjob.entity.Candidate;
import com.smartjob.entity.Skill;
import com.smartjob.entity.User;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.mapper.EntityMapper;
import com.smartjob.repository.CandidateRepository;
import com.smartjob.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing candidate profiles and skills.
 *
 * V1 → V2: Replaces the V1 CandidateService which used HashMap storage.
 * Business logic preserved, data access migrated to JPA repositories.
 */
@Service
public class CandidateServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceV2.class);

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;

    public CandidateServiceV2(CandidateRepository candidateRepository,
                              UserRepository userRepository,
                              SkillService skillService) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.skillService = skillService;
    }

    /**
     * Create a candidate profile for an authenticated user.
     */
    @Transactional
    public CandidateResponse createCandidate(Long userId, CandidateCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (candidateRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Candidate profile already exists for this user");
        }

        Candidate candidate = new Candidate(
            user,
            request.getEducation(),
            request.getExperience(),
            request.getPreferredRole(),
            request.getPreferredLocation()
        );

        // Resolve skill names to Skill entities
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            Set<Skill> skills = skillService.findOrCreateAll(request.getSkills());
            candidate.setSkills(skills);
        }

        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate profile created for user: {} (candidateId={})", user.getEmail(), saved.getId());
        return EntityMapper.toCandidateResponse(saved);
    }

    /**
     * Get candidate by ID.
     */
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = findCandidateEntity(id);
        return EntityMapper.toCandidateResponse(candidate);
    }

    /**
     * Get candidate by user ID.
     */
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateByUserId(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));
        return EntityMapper.toCandidateResponse(candidate);
    }

    /**
     * Update candidate profile.
     */
    @Transactional
    public CandidateResponse updateCandidate(Long id, CandidateUpdateRequest request) {
        Candidate candidate = findCandidateEntity(id);

        if (request.getEducation() != null) {
            candidate.setEducation(request.getEducation());
        }
        if (request.getExperience() != null) {
            candidate.setExperience(request.getExperience());
        }
        if (request.getPreferredRole() != null) {
            candidate.setPreferredRole(request.getPreferredRole());
        }
        if (request.getPreferredLocation() != null) {
            candidate.setPreferredLocation(request.getPreferredLocation());
        }
        if (request.getSkills() != null) {
            Set<Skill> skills = skillService.findOrCreateAll(request.getSkills());
            candidate.setSkills(skills);
        }

        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate profile updated: candidateId={}", saved.getId());
        return EntityMapper.toCandidateResponse(saved);
    }

    /**
     * Delete candidate profile.
     */
    @Transactional
    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }
        candidateRepository.deleteById(id);
        log.info("Candidate profile deleted: candidateId={}", id);
    }

    /**
     * Add a skill to candidate.
     */
    @Transactional
    public CandidateResponse addSkill(Long candidateId, String skillName) {
        Candidate candidate = findCandidateEntity(candidateId);
        Skill skill = skillService.findOrCreate(skillName);
        candidate.addSkill(skill);
        Candidate saved = candidateRepository.save(candidate);
        return EntityMapper.toCandidateResponse(saved);
    }

    /**
     * Remove a skill from candidate.
     */
    @Transactional
    public CandidateResponse removeSkill(Long candidateId, Long skillId) {
        Candidate candidate = findCandidateEntity(candidateId);
        Skill skill = skillService.findById(skillId)
            .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
        candidate.removeSkill(skill);
        Candidate saved = candidateRepository.save(candidate);
        return EntityMapper.toCandidateResponse(saved);
    }

    /**
     * Get candidate skills as string names.
     */
    @Transactional(readOnly = true)
    public List<String> getCandidateSkillNames(Long candidateId) {
        Candidate candidate = findCandidateEntity(candidateId);
        return candidate.getSkills().stream()
            .map(Skill::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Internal helper — finds entity or throws 404.
     */
    public Candidate findCandidateEntity(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
    }

    /**
     * Get all candidates (admin/listing use).
     */
    @Transactional(readOnly = true)
    public List<CandidateResponse> getAllCandidates() {
        return candidateRepository.findAll().stream()
            .map(EntityMapper::toCandidateResponse)
            .collect(Collectors.toList());
    }
}
