package com.smartjob.service;

import com.smartjob.dto.response.ApplicationResponse;
import com.smartjob.entity.Application;
import com.smartjob.entity.Candidate;
import com.smartjob.entity.Job;
import com.smartjob.entity.enums.ApplicationStatus;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.mapper.EntityMapper;
import com.smartjob.repository.ApplicationRepository;
import com.smartjob.repository.CandidateRepository;
import com.smartjob.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing job applications and status updates.
 *
 * V1 → V2: Replaces ApplicationService's HashMap with JPA.
 * Duplicate prevention preserved (V1 used HashMap check, V2 uses both
 * service-level check AND database unique constraint).
 */
@Service
public class ApplicationServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceV2.class);

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    public ApplicationServiceV2(ApplicationRepository applicationRepository,
                                CandidateRepository candidateRepository,
                                JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Apply for a job.
     * V1 equivalent: ApplicationService.apply() — duplicate check preserved.
     */
    @Transactional
    public ApplicationResponse apply(Long candidateId, Long jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Duplicate prevention — V1 logic: "Candidate has already applied to this job"
        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new DuplicateResourceException(
                "Candidate has already applied to this job"
            );
        }

        Application application = new Application(candidate, job);
        Application saved = applicationRepository.save(application);

        log.info("Application created: candidateId={} applied to jobId={} (applicationId={})",
            candidateId, jobId, saved.getId());
        return EntityMapper.toApplicationResponse(saved);
    }

    /**
     * Get application by ID.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        Application app = findApplicationEntity(id);
        return EntityMapper.toApplicationResponse(app);
    }

    /**
     * Get all applications by a candidate.
     * V1 equivalent: ApplicationService.getApplicationsByCandidate()
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByCandidate(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate", "id", candidateId);
        }
        return applicationRepository.findByCandidateId(candidateId).stream()
            .map(EntityMapper::toApplicationResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all applications for a job.
     * V1 equivalent: ApplicationService.getApplicationsByJob()
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByJob(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job", "id", jobId);
        }
        return applicationRepository.findByJobId(jobId).stream()
            .map(EntityMapper::toApplicationResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update application status.
     * V1 equivalent: ApplicationService.updateStatus()
     */
    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, String statusString) {
        Application application = findApplicationEntity(applicationId);

        ApplicationStatus newStatus = ApplicationStatus.fromString(statusString);
        if (newStatus == null) {
            throw new InvalidRequestException(
                "Invalid application status: '" + statusString +
                "'. Valid values: APPLIED, SHORTLISTED, INTERVIEW, REJECTED, SELECTED"
            );
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);

        log.info("Application status updated: applicationId={}, {} → {}",
            applicationId, oldStatus, newStatus);
        return EntityMapper.toApplicationResponse(saved);
    }

    /**
     * Internal helper — finds entity or throws 404.
     */
    private Application findApplicationEntity(Long id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }
}
