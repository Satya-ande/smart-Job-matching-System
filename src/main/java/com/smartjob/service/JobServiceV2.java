package com.smartjob.service;

import com.smartjob.dto.request.JobCreateRequest;
import com.smartjob.dto.request.JobUpdateRequest;
import com.smartjob.dto.response.JobResponse;
import com.smartjob.entity.Job;
import com.smartjob.entity.JobSkill;
import com.smartjob.entity.Skill;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.JobType;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.mapper.EntityMapper;
import com.smartjob.repository.JobRepository;
import com.smartjob.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing job postings, search, and filtering.
 *
 * V1 → V2: Replaces JobService's HashMap with JPA.
 * Search/filter logic migrated from in-memory iteration to database queries.
 * Pagination and sorting added (V1 returned all results).
 */
@Service
public class JobServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(JobServiceV2.class);

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;

    // Whitelist of allowed sort fields to prevent SQL injection
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
        "salaryMin", "salaryMax", "createdAt", "experienceRequired", "title", "company"
    );

    public JobServiceV2(JobRepository jobRepository, UserRepository userRepository,
                        SkillService skillService) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.skillService = skillService;
    }

    /**
     * Create a new job posting.
     */
    @Transactional
    public JobResponse createJob(Long recruiterId, JobCreateRequest request) {
        User recruiter = userRepository.findById(recruiterId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));

        JobType jobType = null;
        if (request.getJobType() != null) {
            jobType = JobType.fromString(request.getJobType());
            if (jobType == null) {
                throw new InvalidRequestException("Invalid job type: " + request.getJobType());
            }
        }

        Job job = new Job(
            request.getTitle(),
            request.getCompany(),
            request.getLocation(),
            request.getExperienceRequired(),
            request.getSalaryMin(),
            request.getSalaryMax(),
            jobType,
            request.getDescription(),
            recruiter
        );

        // Add skill associations
        if (request.getSkills() != null) {
            for (JobCreateRequest.JobSkillRequest skillReq : request.getSkills()) {
                Skill skill = skillService.findOrCreate(skillReq.getName());
                int weight = skillReq.getWeight() > 0 ? skillReq.getWeight()
                    : (skillReq.isRequired() ? 5 : 2); // V1 defaults
                JobSkill jobSkill = new JobSkill(job, skill, skillReq.isRequired(), weight);
                job.addJobSkill(jobSkill);
            }
        }

        Job saved = jobRepository.save(job);
        log.info("Job created: '{}' at {} (jobId={})", saved.getTitle(), saved.getCompany(), saved.getId());
        return EntityMapper.toJobResponse(saved);
    }

    /**
     * Get job by ID.
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Job job = findJobEntity(id);
        return EntityMapper.toJobResponse(job);
    }

    /**
     * Get all jobs with pagination.
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable).map(EntityMapper::toJobResponse);
    }

    /**
     * Search jobs by keyword across title, company, description, and skills.
     * V1 equivalent: JobService.searchByTitle() + searchByCompany() + searchBySkill()
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllJobs(pageable);
        }
        return jobRepository.searchByKeyword(keyword.trim(), pageable)
            .map(EntityMapper::toJobResponse);
    }

    /**
     * Dynamic search and multi-criteria filtering using JPA Specifications.
     * Implements Section 16 & 17.
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> filterJobs(
            String keyword,
            String location,
            Double minSalary,
            Double maxSalary,
            Double experienceRequired,
            JobType jobType,
            String skill,
            Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Job> spec =
            com.smartjob.specification.JobSpecification.filterJobs(
                keyword, location, minSalary, maxSalary, experienceRequired, jobType, skill
            );

        return jobRepository.findAll(spec, pageable).map(EntityMapper::toJobResponse);
    }

    /**
     * Update an existing job.
     */
    @Transactional
    public JobResponse updateJob(Long id, JobUpdateRequest request) {
        Job job = findJobEntity(id);

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getCompany() != null) job.setCompany(request.getCompany());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getExperienceRequired() != null) job.setExperienceRequired(request.getExperienceRequired());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getJobType() != null) {
            JobType jobType = JobType.fromString(request.getJobType());
            if (jobType == null) {
                throw new InvalidRequestException("Invalid job type: " + request.getJobType());
            }
            job.setJobType(jobType);
        }

        // Update skills if provided
        if (request.getSkills() != null) {
            job.clearJobSkills();
            for (JobCreateRequest.JobSkillRequest skillReq : request.getSkills()) {
                Skill skill = skillService.findOrCreate(skillReq.getName());
                int weight = skillReq.getWeight() > 0 ? skillReq.getWeight()
                    : (skillReq.isRequired() ? 5 : 2);
                JobSkill jobSkill = new JobSkill(job, skill, skillReq.isRequired(), weight);
                job.addJobSkill(jobSkill);
            }
        }

        Job saved = jobRepository.save(job);
        log.info("Job updated: jobId={}", saved.getId());
        return EntityMapper.toJobResponse(saved);
    }

    /**
     * Delete a job.
     */
    @Transactional
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job", "id", id);
        }
        jobRepository.deleteById(id);
        log.info("Job deleted: jobId={}", id);
    }

    /**
     * Get jobs by recruiter.
     */
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsByRecruiter(Long recruiterId) {
        return jobRepository.findByRecruiterId(recruiterId).stream()
            .map(EntityMapper::toJobResponse)
            .collect(Collectors.toList());
    }

    /**
     * Internal helper — finds entity or throws 404.
     */
    public Job findJobEntity(Long id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
    }

    /**
     * Get all job entities (for matching engine).
     */
    @Transactional(readOnly = true)
    public List<Job> findAllJobEntities() {
        return jobRepository.findAll();
    }

    /**
     * Validate sort field against whitelist.
     */
    public static boolean isAllowedSortField(String field) {
        return ALLOWED_SORT_FIELDS.contains(field);
    }
}
