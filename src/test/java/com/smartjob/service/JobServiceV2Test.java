package com.smartjob.service;

import com.smartjob.dto.request.JobCreateRequest;
import com.smartjob.dto.request.JobUpdateRequest;
import com.smartjob.dto.response.JobResponse;
import com.smartjob.entity.Job;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.JobType;
import com.smartjob.entity.enums.Role;
import com.smartjob.exception.ForbiddenException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.JobRepository;
import com.smartjob.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobServiceV2 — Job Management & Filtering Tests")
class JobServiceV2Test {

    @Mock private JobRepository jobRepository;
    @Mock private UserRepository userRepository;
    @Mock private SkillService skillService;

    @InjectMocks private JobServiceV2 jobService;

    private User recruiter;
    private User otherRecruiter;
    private Job mockJob;

    @BeforeEach
    void setUp() {
        recruiter = new User("Tech Recruiter", "recruiter@smartjob.com", "pass", Role.RECRUITER);
        recruiter.setId(10L);

        otherRecruiter = new User("Other Recruiter", "other@smartjob.com", "pass", Role.RECRUITER);
        otherRecruiter.setId(20L);

        mockJob = new Job(
            "Java Backend Developer", "TCS", "Hyderabad",
            3.0, 600000.0, 1200000.0, JobType.FULL_TIME, "Java role", recruiter
        );
        mockJob.setId(101L);
    }

    @Test
    @DisplayName("createJob — succeeds with valid recruiter and skills")
    void createJob_success() {
        JobCreateRequest request = new JobCreateRequest();
        request.setTitle("Java Backend Developer");
        request.setCompany("TCS");
        request.setLocation("Hyderabad");
        request.setExperienceRequired(3.0);
        request.setSalaryMin(600000.0);
        request.setSalaryMax(1200000.0);
        request.setJobType("FULL_TIME");

        when(userRepository.findById(10L)).thenReturn(Optional.of(recruiter));
        when(jobRepository.save(any(Job.class))).thenReturn(mockJob);

        JobResponse response = jobService.createJob(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Java Backend Developer");
        assertThat(response.getCompany()).isEqualTo("TCS");
    }

    @Test
    @DisplayName("getJobById — returns job when found")
    void getJobById_success() {
        when(jobRepository.findById(101L)).thenReturn(Optional.of(mockJob));

        JobResponse response = jobService.getJobById(101L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("getJobById — throws ResourceNotFoundException when not found")
    void getJobById_notFound() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateJob — succeeds when caller is job owner")
    void updateJob_ownershipSuccess() {
        JobUpdateRequest updateReq = new JobUpdateRequest();
        updateReq.setTitle("Senior Java Developer");

        when(jobRepository.findById(101L)).thenReturn(Optional.of(mockJob));
        when(jobRepository.save(any(Job.class))).thenReturn(mockJob);

        JobResponse response = jobService.updateJob(101L, 10L, updateReq);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("updateJob — throws ForbiddenException when caller does not own job")
    void updateJob_forbidden() {
        JobUpdateRequest updateReq = new JobUpdateRequest();
        updateReq.setTitle("Hacked Title");

        when(jobRepository.findById(101L)).thenReturn(Optional.of(mockJob));

        assertThatThrownBy(() -> jobService.updateJob(101L, 20L, updateReq))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("not authorized to modify");
    }

    @Test
    @DisplayName("deleteJob — throws ForbiddenException when caller does not own job")
    void deleteJob_forbidden() {
        when(jobRepository.findById(101L)).thenReturn(Optional.of(mockJob));

        assertThatThrownBy(() -> jobService.deleteJob(101L, 20L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("not authorized to delete");
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("filterJobs — returns paged results using JobSpecification")
    void filterJobs_success() {
        Page<Job> jobPage = new PageImpl<>(List.of(mockJob));
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(jobPage);

        Page<JobResponse> results = jobService.filterJobs(
            "Java", "Hyderabad", 500000.0, 1500000.0, 3.0, JobType.FULL_TIME, "Java", PageRequest.of(0, 10)
        );

        assertThat(results).isNotNull();
        assertThat(results.getContent()).hasSize(1);
    }
}
