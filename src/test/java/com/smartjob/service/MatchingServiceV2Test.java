package com.smartjob.service;

import com.smartjob.dto.response.MatchResultResponse;
import com.smartjob.entity.*;
import com.smartjob.entity.enums.JobType;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for MatchingServiceV2.
 *
 * V1 → V2: V1 had TestRunner with manual asserts.
 * V2 uses JUnit 5 + Mockito — repositories are mocked, algorithm is tested in isolation.
 *
 * Key invariants verified (identical to V1 JobMatcherTest):
 *   - Required skill weight = 5, Preferred = 2
 *   - Composite formula = 70% skill + 20% experience + 10% location
 *   - 100% match when all skills match and experience/location are perfect
 *   - Remote jobs give 100% location score
 *   - Missing required skills reduce skill score proportionally
 *   - PriorityQueue ranking produces correct top-K order
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingServiceV2 — Job Matching Algorithm Tests")
class MatchingServiceV2Test {

    @Mock private CandidateRepository candidateRepository;
    @Mock private JobRepository jobRepository;

    @InjectMocks private MatchingServiceV2 matchingService;

    // Test data
    private User testUser;
    private Candidate candidate;
    private Skill java, spring, sql, docker, aws;
    private Job javaJob, devopsJob, internJob;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com", "password", null);

        // Skills
        java   = skillWithId(1L, "Java");
        spring = skillWithId(2L, "Spring Boot");
        sql    = skillWithId(3L, "SQL");
        docker = skillWithId(4L, "Docker");
        aws    = skillWithId(5L, "AWS");

        // Candidate: Java, Spring, SQL
        candidate = new Candidate(testUser, "B.Tech", 2.0, "Java Developer", "Hyderabad");
        candidate.setSkills(new HashSet<>(Arrays.asList(java, spring, sql)));

        // Job 1: requires Java(5), Spring(5), SQL(5); prefers Docker(2), Git(2) — total 19pts
        javaJob = buildJob(1L, "Java Backend Developer", "TCS", "Hyderabad",
            3.0, 600000, 1000000, JobType.FULL_TIME,
            new JobSkillDef(java, true), new JobSkillDef(spring, true),
            new JobSkillDef(sql, true), new JobSkillDef(docker, false));

        // Job 2: requires AWS(5), Docker(5) — candidate has neither — total 10pts
        devopsJob = buildJob(2L, "DevOps Engineer", "Amazon", "Hyderabad",
            3.0, 1200000, 2000000, JobType.REMOTE,
            new JobSkillDef(aws, true), new JobSkillDef(docker, true));

        // Job 3: requires Java(5); prefers SQL(2), Git(2) — Remote — 0 exp required
        internJob = buildJob(3L, "Backend Intern", "Startup", "Remote",
            0.0, 300000, 500000, JobType.INTERNSHIP,
            new JobSkillDef(java, true), new JobSkillDef(sql, false));
    }

    // ==========================================
    // SKILL SCORE TESTS
    // ==========================================

    @Test
    @DisplayName("100% match when candidate has all required and preferred skills")
    void perfectSkillMatch_returns100Score() {
        Set<String> skills = Set.of("Java", "Spring Boot", "SQL", "Docker");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 3.0, "Hyderabad", javaJob);

        assertThat(result.getSkillScore()).isEqualTo(100.0);
        assertThat(result.getMatchScore()).isEqualTo(100.0);
        assertThat(result.getMissingRequiredSkills()).isEmpty();
        assertThat(result.getMissingPreferredSkills()).isEmpty();
    }

    @Test
    @DisplayName("Skill score calculated correctly with required=5 and preferred=2 weights")
    void skillScoreWeights_requiredFive_preferredTwo() {
        // candidate has Java(5), Spring(5), SQL(5) — missing Docker(2). Total=19, earned=15
        Set<String> candidateSkills = Set.of("Java", "Spring Boot", "SQL");
        MatchResultResponse result = matchingService.matchCandidateToJob(candidateSkills, 3.0, "Hyderabad", javaJob);

        assertThat(result.getEarnedSkillPoints()).isEqualTo(15); // 5+5+5
        assertThat(result.getTotalSkillPoints()).isEqualTo(17);  // 5+5+5+2
        assertThat(result.getSkillScore()).isCloseTo(88.24, within(0.1));
        assertThat(result.getMatchingRequiredSkills()).containsExactlyInAnyOrder("Java", "Spring Boot", "SQL");
        assertThat(result.getMissingPreferredSkills()).contains("Docker");
    }

    @Test
    @DisplayName("0% skill score when candidate has none of the required skills")
    void noMatchingSkills_returnsZeroSkillScore() {
        Set<String> noSkills = Collections.emptySet();
        MatchResultResponse result = matchingService.matchCandidateToJob(noSkills, 5.0, "Hyderabad", devopsJob);

        assertThat(result.getSkillScore()).isEqualTo(0.0);
        assertThat(result.getEarnedSkillPoints()).isEqualTo(0);
        assertThat(result.getMissingRequiredSkills()).containsExactlyInAnyOrder("AWS", "Docker");
    }

    @Test
    @DisplayName("Skill matching is case-insensitive")
    void skillMatching_isCaseInsensitive() {
        // V1 behavior: 'java' should match 'Java'
        Set<String> lowerCaseSkills = Set.of("java", "spring boot", "sql");
        MatchResultResponse result = matchingService.matchCandidateToJob(lowerCaseSkills, 3.0, "Hyderabad", javaJob);

        assertThat(result.getMatchingRequiredSkills()).hasSize(3);
        assertThat(result.getMissingRequiredSkills()).isEmpty();
    }

    @Test
    @DisplayName("100% skill score when job has no skills defined")
    void noJobSkills_returns100SkillScore() {
        Job emptyJob = buildJob(99L, "Generic Role", "Corp", "Remote",
            0.0, 0, 0, JobType.FULL_TIME); // no skills

        Set<String> skills = Set.of("Java");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 1.0, "Remote", emptyJob);

        assertThat(result.getSkillScore()).isEqualTo(100.0);
        assertThat(result.getTotalSkillPoints()).isEqualTo(0);
    }

    // ==========================================
    // EXPERIENCE SCORE TESTS
    // ==========================================

    @Test
    @DisplayName("100% experience score when candidate meets requirement exactly")
    void experienceScore_exact_returns100() {
        Set<String> skills = Set.of("Java", "Spring Boot", "SQL");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 3.0, "Hyderabad", javaJob);

        assertThat(result.getExperienceScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("100% experience score when candidate exceeds requirement")
    void experienceScore_exceeds_returns100() {
        Set<String> skills = Set.of("Java", "Spring Boot", "SQL");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 10.0, "Hyderabad", javaJob);

        assertThat(result.getExperienceScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Proportional experience score when candidate is under-qualified")
    void experienceScore_partial_isProportional() {
        // 2.0 years / 3.0 required = 66.67%
        Set<String> skills = Set.of("Java");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 2.0, "Hyderabad", javaJob);

        assertThat(result.getExperienceScore()).isCloseTo(66.67, within(0.01));
    }

    @Test
    @DisplayName("100% experience score when job requires 0 experience")
    void experienceScore_zeroRequired_returns100() {
        Set<String> skills = Set.of("Java");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 0.0, "Remote", internJob);

        assertThat(result.getExperienceScore()).isEqualTo(100.0);
    }

    // ==========================================
    // LOCATION SCORE TESTS
    // ==========================================

    @Test
    @DisplayName("100% location score on exact match")
    void locationScore_exactMatch_returns100() {
        MatchResultResponse result = matchingService.matchCandidateToJob(
            Set.of("Java"), 5.0, "Hyderabad", javaJob);

        assertThat(result.getLocationScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("100% location score for Remote jobs")
    void locationScore_remote_returns100() {
        MatchResultResponse result = matchingService.matchCandidateToJob(
            Set.of("AWS", "Docker"), 5.0, "Hyderabad", devopsJob); // devopsJob is REMOTE

        assertThat(result.getLocationScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("0% location score on different cities")
    void locationScore_differentCity_returnsZero() {
        // candidate in Hyderabad, job in Pune
        Job puneJob = buildJob(10L, "Dev", "Co", "Pune", 1.0, 0, 0, JobType.FULL_TIME,
            new JobSkillDef(java, true));
        MatchResultResponse result = matchingService.matchCandidateToJob(
            Set.of("Java"), 2.0, "Hyderabad", puneJob);

        assertThat(result.getLocationScore()).isEqualTo(0.0);
    }

    // ==========================================
    // COMPOSITE SCORE TESTS
    // ==========================================

    @Test
    @DisplayName("Composite score = 70%*skill + 20%*exp + 10%*location")
    void compositeScore_formula_isCorrect() {
        // candidate has Java, Spring, SQL — missing Docker (2pts)
        // skill = 15/17*100 = 88.24
        // exp = 2/3*100 = 66.67
        // location = 100.0 (Hyderabad match)
        // composite = 0.70*88.24 + 0.20*66.67 + 0.10*100.0 = 61.77 + 13.33 + 10.0 = 85.1
        Set<String> skills = Set.of("Java", "Spring Boot", "SQL");
        MatchResultResponse result = matchingService.matchCandidateToJob(skills, 2.0, "Hyderabad", javaJob);

        double expected = (0.70 * result.getSkillScore())
                        + (0.20 * result.getExperienceScore())
                        + (0.10 * result.getLocationScore());
        assertThat(result.getMatchScore()).isCloseTo(expected, within(0.01));
    }

    // ==========================================
    // RANKING (PriorityQueue) TESTS
    // ==========================================

    @Test
    @DisplayName("getMatchingJobs returns results ranked by matchScore descending")
    void getMatchingJobs_resultsAreRankedDescending() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findAll()).thenReturn(List.of(javaJob, devopsJob, internJob));

        List<MatchResultResponse> results = matchingService.getMatchingJobs(1L, 3);

        assertThat(results).hasSize(3);
        // Scores should be in descending order
        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).getMatchScore())
                .isGreaterThanOrEqualTo(results.get(i + 1).getMatchScore());
        }
    }

    @Test
    @DisplayName("getMatchingJobs respects topK limit")
    void getMatchingJobs_respectsTopKLimit() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findAll()).thenReturn(List.of(javaJob, devopsJob, internJob));

        List<MatchResultResponse> results = matchingService.getMatchingJobs(1L, 2);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("intern job (100% match) ranks first for Java/SQL candidate")
    void perfectMatchJob_ranksFirst() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findAll()).thenReturn(List.of(javaJob, devopsJob, internJob));

        List<MatchResultResponse> results = matchingService.getMatchingJobs(1L, 3);

        // internJob: Java required (have it) + SQL preferred (have it), 0 exp needed, Remote=100%
        assertThat(results.get(0).getJobId()).isEqualTo(3L);
        assertThat(results.get(0).getMatchScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("DevOps job with no matching required skills ranks last")
    void noSkillMatch_ranksLast() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findAll()).thenReturn(List.of(javaJob, devopsJob, internJob));

        List<MatchResultResponse> results = matchingService.getMatchingJobs(1L, 3);

        assertThat(results.get(results.size() - 1).getJobId()).isEqualTo(2L); // devops
    }

    @Test
    @DisplayName("empty job list returns empty results")
    void emptyJobList_returnsEmpty() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findAll()).thenReturn(Collections.emptyList());

        List<MatchResultResponse> results = matchingService.getMatchingJobs(1L, 5);

        assertThat(results).isEmpty();
    }

    // ==========================================
    // SKILL GAP ANALYSIS TESTS
    // ==========================================

    @Test
    @DisplayName("getSkillGap identifies missing required skills")
    void skillGap_identifiesMissingRequired() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(devopsJob));

        MatchResultResponse result = matchingService.getSkillGap(1L, 2L);

        // candidate has Java, Spring, SQL — devops needs AWS and Docker
        assertThat(result.getMissingRequiredSkills()).containsExactlyInAnyOrder("AWS", "Docker");
        assertThat(result.getMatchingRequiredSkills()).isEmpty();
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Skill skillWithId(Long id, String name) {
        Skill s = new Skill(name);
        // Use reflection to set id since it's JPA-managed
        try {
            var field = Skill.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(s, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    private record JobSkillDef(Skill skill, boolean required) {}

    private Job buildJob(Long id, String title, String company, String location,
                         double expRequired, double salMin, double salMax,
                         JobType type, JobSkillDef... skills) {
        Job job = new Job(title, company, location, expRequired, salMin, salMax, type, null, null);
        try {
            var field = Job.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(job, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (JobSkillDef sd : skills) {
            int weight = sd.required() ? MatchingServiceV2.REQUIRED_SKILL_WEIGHT
                                       : MatchingServiceV2.PREFERRED_SKILL_WEIGHT;
            JobSkill js = new JobSkill(job, sd.skill(), sd.required(), weight);
            job.addJobSkill(js);
        }
        return job;
    }
}
