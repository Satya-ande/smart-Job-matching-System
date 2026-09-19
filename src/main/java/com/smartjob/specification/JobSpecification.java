package com.smartjob.specification;

import com.smartjob.entity.Job;
import com.smartjob.entity.JobSkill;
import com.smartjob.entity.Skill;
import com.smartjob.entity.enums.JobType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic JPA Specification builder for Job search and multi-criteria filtering.
 *
 * Implements Section 17 of V2 specification:
 * Supports dynamic combinations of keyword, location, salary range,
 * required experience, job type, and required/preferred skills.
 */
public final class JobSpecification {

    private JobSpecification() {
        // Utility class
    }

    public static Specification<Job> filterJobs(
            String keyword,
            String location,
            Double minSalary,
            Double maxSalary,
            Double experienceRequired,
            JobType jobType,
            String skill) {

        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search across title, company, description
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), pattern);
                Predicate companyPred = cb.like(cb.lower(root.get("company")), pattern);
                Predicate descPred = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titlePred, companyPred, descPred));
            }

            // 2. Location filter (case-insensitive substring match)
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("location")),
                    "%" + location.trim().toLowerCase() + "%"
                ));
            }

            // 3. Minimum salary filter (job max salary >= requested min salary)
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMax"), minSalary));
            }

            // 4. Maximum salary filter
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMin"), maxSalary));
            }

            // 5. Experience required filter (jobs requiring <= candidate experience)
            if (experienceRequired != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceRequired"), experienceRequired));
            }

            // 6. Job type filter (enum)
            if (jobType != null) {
                predicates.add(cb.equal(root.get("jobType"), jobType));
            }

            // 7. Skill filter (via join on job_skills and skills)
            if (skill != null && !skill.trim().isEmpty()) {
                Join<Job, JobSkill> jobSkillJoin = root.join("jobSkills", JoinType.LEFT);
                Join<JobSkill, Skill> skillJoin = jobSkillJoin.join("skill", JoinType.LEFT);
                predicates.add(cb.like(
                    cb.lower(skillJoin.get("name")),
                    "%" + skill.trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
