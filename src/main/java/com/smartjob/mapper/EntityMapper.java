package com.smartjob.mapper;

import com.smartjob.dto.response.*;
import com.smartjob.entity.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting between JPA entities and DTOs.
 *
 * Centralizes all entity-to-DTO transformations to keep controllers and services clean.
 * Prevents JPA entities from being exposed directly in API responses.
 */
public final class EntityMapper {

    private EntityMapper() {
        // Utility class — prevent instantiation
    }

    // ==========================================
    // CANDIDATE MAPPING
    // ==========================================

    /**
     * Convert Candidate entity to CandidateResponse DTO.
     * Flattens User (name, email) and Skill entities (to names).
     */
    public static CandidateResponse toCandidateResponse(Candidate candidate) {
        if (candidate == null) return null;

        CandidateResponse response = new CandidateResponse();
        response.setId(candidate.getId());
        response.setEducation(candidate.getEducation());
        response.setExperience(candidate.getExperience());
        response.setPreferredRole(candidate.getPreferredRole());
        response.setPreferredLocation(candidate.getPreferredLocation());
        response.setCreatedAt(candidate.getCreatedAt());
        response.setUpdatedAt(candidate.getUpdatedAt());

        // Flatten User data
        if (candidate.getUser() != null) {
            response.setName(candidate.getUser().getName());
            response.setEmail(candidate.getUser().getEmail());
        }

        // Convert Skill entities to name strings
        if (candidate.getSkills() != null) {
            response.setSkills(
                candidate.getSkills().stream()
                    .map(Skill::getName)
                    .sorted()
                    .collect(Collectors.toList())
            );
        }

        return response;
    }

    // ==========================================
    // JOB MAPPING
    // ==========================================

    /**
     * Convert Job entity to JobResponse DTO.
     * Converts JobSkill entities to readable skill responses.
     */
    public static JobResponse toJobResponse(Job job) {
        if (job == null) return null;

        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setCompany(job.getCompany());
        response.setLocation(job.getLocation());
        response.setExperienceRequired(job.getExperienceRequired());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setJobType(job.getJobType() != null ? job.getJobType().getDisplayName() : null);
        response.setDescription(job.getDescription());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());

        // Flatten recruiter name
        if (job.getRecruiter() != null) {
            response.setRecruiterName(job.getRecruiter().getName());
        }

        // Convert JobSkill entities to responses
        if (job.getJobSkills() != null) {
            List<JobResponse.JobSkillResponse> skillResponses = job.getJobSkills().stream()
                .map(js -> new JobResponse.JobSkillResponse(
                    js.getSkill().getName(),
                    js.isRequired(),
                    js.getWeight()
                ))
                .collect(Collectors.toList());
            response.setSkills(skillResponses);
        }

        return response;
    }

    // ==========================================
    // APPLICATION MAPPING
    // ==========================================

    /**
     * Convert Application entity to ApplicationResponse DTO.
     * Denormalizes candidate name and job title.
     */
    public static ApplicationResponse toApplicationResponse(Application application) {
        if (application == null) return null;

        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setAppliedDate(application.getAppliedDate());
        response.setStatus(application.getStatus().getDisplayName());
        response.setUpdatedAt(application.getUpdatedAt());

        if (application.getCandidate() != null) {
            response.setCandidateId(application.getCandidate().getId());
            if (application.getCandidate().getUser() != null) {
                response.setCandidateName(application.getCandidate().getUser().getName());
            }
        }

        if (application.getJob() != null) {
            response.setJobId(application.getJob().getId());
            response.setJobTitle(application.getJob().getTitle());
            response.setCompany(application.getJob().getCompany());
        }

        return response;
    }

    // ==========================================
    // SKILL MAPPING
    // ==========================================

    public static SkillResponse toSkillResponse(Skill skill) {
        if (skill == null) return null;
        return new SkillResponse(skill.getId(), skill.getName());
    }
}
