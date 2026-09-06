package com.smartjob.service;

import com.smartjob.dto.response.ApplicationResponse;
import com.smartjob.entity.*;
import com.smartjob.entity.enums.ApplicationStatus;
import com.smartjob.entity.enums.JobType;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.ApplicationRepository;
import com.smartjob.repository.CandidateRepository;
import com.smartjob.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationServiceV2.
 * Tests apply, duplicate prevention, and status updates.
 *
 * V1 equivalents: ApplicationService tests in V1 TestRunner.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationServiceV2 — Application Management Tests")
class ApplicationServiceV2Test {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private JobRepository jobRepository;

    @InjectMocks private ApplicationServiceV2 applicationService;

    private Candidate candidate;
    private Job job;
    private Application application;

    @BeforeEach
    void setUp() {
        User user = new User("Test User", "test@example.com", "pass", null);
        candidate = new Candidate(user, "B.Tech", 2.0, "Dev", "Hyderabad");
        job = new Job("Java Dev", "TCS", "Hyderabad", 2.0, 500000, 900000,
            JobType.FULL_TIME, "Java job", null);
        application = new Application(candidate, job);
    }

    @Test
    @DisplayName("apply — creates application successfully on first apply")
    void apply_success_createsApplication() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 1L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        ApplicationResponse response = applicationService.apply(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("Applied");
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    @DisplayName("apply — throws DuplicateResourceException on second apply to same job")
    void apply_duplicate_throwsException() {
        // V1 preserved: "Candidate has already applied to this job"
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(1L, 1L))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already applied");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("apply — throws ResourceNotFoundException for unknown candidate")
    void apply_candidateNotFound_throwsException() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.apply(99L, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Candidate");
    }

    @Test
    @DisplayName("apply — throws ResourceNotFoundException for unknown job")
    void apply_jobNotFound_throwsException() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.apply(1L, 99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Job");
    }

    @Test
    @DisplayName("updateStatus — transitions status correctly to SHORTLISTED")
    void updateStatus_shortlisted_updatesCorrectly() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        ApplicationResponse response = applicationService.updateStatus(1L, "SHORTLISTED");

        assertThat(response.getStatus()).isEqualTo("Shortlisted");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
    }

    @Test
    @DisplayName("updateStatus — throws InvalidRequestException for unknown status")
    void updateStatus_invalidStatus_throwsException() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateStatus(1L, "FLYING"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("Invalid application status");
    }

    @Test
    @DisplayName("getApplicationsByCandidate — returns all applications for candidate")
    void getApplicationsByCandidate_returnsList() {
        when(candidateRepository.existsById(1L)).thenReturn(true);
        when(applicationRepository.findByCandidateId(1L)).thenReturn(List.of(application));

        List<ApplicationResponse> responses = applicationService.getApplicationsByCandidate(1L);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("getApplicationsByCandidate — throws ResourceNotFoundException for unknown candidate")
    void getApplicationsByCandidate_candidateNotFound_throwsException() {
        when(candidateRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> applicationService.getApplicationsByCandidate(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
