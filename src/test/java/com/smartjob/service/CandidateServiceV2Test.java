package com.smartjob.service;

import com.smartjob.dto.request.CandidateCreateRequest;
import com.smartjob.dto.response.CandidateResponse;
import com.smartjob.entity.Candidate;
import com.smartjob.entity.Skill;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.Role;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.CandidateRepository;
import com.smartjob.repository.UserRepository;
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
 * Unit tests for CandidateServiceV2.
 * Tests CRUD operations and business rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CandidateServiceV2 — Candidate Management Tests")
class CandidateServiceV2Test {

    @Mock private CandidateRepository candidateRepository;
    @Mock private UserRepository userRepository;
    @Mock private SkillService skillService;

    @InjectMocks private CandidateServiceV2 candidateService;

    private User mockUser;
    private Candidate mockCandidate;

    @BeforeEach
    void setUp() {
        mockUser = new User("Satya Ande", "satya@example.com",
            "$2a$10$hashedpassword", Role.CANDIDATE);

        mockCandidate = new Candidate(mockUser, "B.Tech CS", 2.0,
            "Java Developer", "Hyderabad");
        mockCandidate.setSkills(new HashSet<>(Arrays.asList(new Skill("Java"), new Skill("SQL"))));
    }

    @Test
    @DisplayName("createCandidate — succeeds for new user with no existing profile")
    void createCandidate_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(candidateRepository.existsByUserId(1L)).thenReturn(false);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(mockCandidate);
        when(skillService.findOrCreateAll(any())).thenReturn(new HashSet<>());

        CandidateCreateRequest request = new CandidateCreateRequest();
        request.setEducation("B.Tech CS");
        request.setExperience(2.0);
        request.setPreferredRole("Java Developer");
        request.setPreferredLocation("Hyderabad");
        request.setSkills(List.of("Java", "SQL"));

        CandidateResponse response = candidateService.createCandidate(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Satya Ande");
        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    @DisplayName("createCandidate — throws DuplicateResourceException if profile already exists")
    void createCandidate_duplicateProfile_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(candidateRepository.existsByUserId(1L)).thenReturn(true);

        CandidateCreateRequest request = new CandidateCreateRequest();
        request.setEducation("B.Tech");
        request.setExperience(1.0);

        assertThatThrownBy(() -> candidateService.createCandidate(1L, request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");

        verify(candidateRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCandidate — throws ResourceNotFoundException for unknown userId")
    void createCandidate_unknownUser_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.createCandidate(99L, new CandidateCreateRequest()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }

    @Test
    @DisplayName("getCandidateById — returns correct candidate")
    void getCandidateById_found_returnsResponse() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(mockCandidate));

        CandidateResponse response = candidateService.getCandidateById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getEducation()).isEqualTo("B.Tech CS");
        assertThat(response.getExperience()).isEqualTo(2.0);
        assertThat(response.getPreferredLocation()).isEqualTo("Hyderabad");
    }

    @Test
    @DisplayName("getCandidateById — throws ResourceNotFoundException for unknown ID")
    void getCandidateById_notFound_throwsException() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.getCandidateById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Candidate")
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteCandidate — throws ResourceNotFoundException for unknown ID")
    void deleteCandidate_notFound_throwsException() {
        when(candidateRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> candidateService.deleteCandidate(99L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(candidateRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("getCandidateSkillNames — returns sorted skill names")
    void getCandidateSkillNames_returnsSortedNames() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(mockCandidate));

        List<String> skillNames = candidateService.getCandidateSkillNames(1L);

        assertThat(skillNames).containsExactly("Java", "SQL"); // sorted alphabetically
        assertThat(skillNames).isSorted();
    }
}
