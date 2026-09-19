package com.smartjob;

import com.smartjob.model.Candidate;
import com.smartjob.model.Job;
import com.smartjob.model.MatchResult;
import com.smartjob.service.JobMatcher;

import java.util.List;
import java.util.Set;

/**
 * Unit tests for JobMatcher scoring, weighting, and PriorityQueue ranking.
 */
public class JobMatcherTest {

    private final JobMatcher matcher = new JobMatcher();

    public void runAllTests() {
        System.out.println("Running JobMatcherTest...");
        testAllRequiredSkillsMatch();
        testNoSkillsMatch();
        testPartialRequiredSkillsMatch();
        testRequiredAndPreferredSkillsMatch();
        testPriorityQueueRankingOrder();
        testSkillGapPrioritization();
        System.out.println("  [PASS] All JobMatcherTest cases passed successfully.\n");
    }

    private void testAllRequiredSkillsMatch() {
        Candidate candidate = new Candidate("C1", "Alice", "alice@test.com", "B.Tech", 2.0, "Java Dev", "Hyderabad",
                Set.of("Java", "SQL", "Spring Boot"));
        Job job = new Job("J1", "Java Dev", "TechCorp", "Hyderabad", 2.0, 600000, 900000,
                Set.of("Java", "SQL", "Spring Boot"), Set.of(), "Full Time", "Desc");

        MatchResult res = matcher.match(candidate, job);
        assert res.getSkillScore() == 100.0 : "Skill score should be 100% when all required skills match";
        assert res.getMissingRequiredSkills().isEmpty() : "Missing required skills should be empty";
        assert res.getMatchingRequiredSkills().size() == 3 : "Matching required skills count should be 3";
        assert res.getMatchScore() == 100.0 : "Overall score should be 100% with full skill, exp, and location match";
    }

    private void testNoSkillsMatch() {
        Candidate candidate = new Candidate("C2", "Bob", "bob@test.com", "B.Tech", 0.0, "Python Dev", "Pune",
                Set.of("C++", "HTML"));
        Job job = new Job("J2", "Java Dev", "TechCorp", "Hyderabad", 2.0, 600000, 900000,
                Set.of("Java", "SQL", "Spring Boot"), Set.of("Docker"), "Full Time", "Desc");

        MatchResult res = matcher.match(candidate, job);
        assert res.getSkillScore() == 0.0 : "Skill score should be 0% when no skills match";
        assert res.getMatchingRequiredSkills().isEmpty() : "Matching required skills should be empty";
        assert res.getMissingRequiredSkills().size() == 3 : "Missing required skills count should be 3";
        assert res.getMissingPreferredSkills().size() == 1 : "Missing preferred skills count should be 1";
    }

    private void testPartialRequiredSkillsMatch() {
        // Job requires: Java (5), SQL (5), Spring Boot (5), REST API (5) -> Total = 20 pts
        // Candidate has: Java, SQL -> Earned = 10 pts -> Skill Score = 10/20 = 50.0%
        Candidate candidate = new Candidate("C3", "Charlie", "charlie@test.com", "B.Tech", 2.0, "Java Dev", "Hyderabad",
                Set.of("Java", "SQL", "Python"));
        Job job = new Job("J3", "Java Dev", "TechCorp", "Hyderabad", 2.0, 600000, 900000,
                Set.of("Java", "SQL", "Spring Boot", "REST API"), Set.of(), "Full Time", "Desc");

        MatchResult res = matcher.match(candidate, job);
        assert res.getSkillScore() == 50.0 : "Skill score should be 50.0% for 2 out of 4 required skills";
        assert res.getMatchingRequiredSkills().contains("Java") : "Should match Java";
        assert res.getMatchingRequiredSkills().contains("SQL") : "Should match SQL";
        assert res.getMissingRequiredSkills().contains("Spring Boot") : "Should miss Spring Boot";
        assert res.getMissingRequiredSkills().contains("REST API") : "Should miss REST API";
    }

    private void testRequiredAndPreferredSkillsMatch() {
        // Required: Java (5), SQL (5), Spring Boot (5) = 15 pts
        // Preferred: Git (2), Docker (2) = 4 pts
        // Total possible = 19 pts
        // Candidate has: Java, SQL, Git -> Earned = 5 + 5 + 2 = 12 pts
        // Skill score = 12 / 19 * 100 = 63.1578... -> 63.16%
        Candidate candidate = new Candidate("C4", "Diana", "diana@test.com", "B.Tech", 2.0, "Dev", "Remote",
                Set.of("Java", "SQL", "Git"));
        Job job = new Job("J4", "Dev", "TechCorp", "Remote", 2.0, 600000, 900000,
                Set.of("Java", "SQL", "Spring Boot"), Set.of("Git", "Docker"), "Full Time", "Desc");

        MatchResult res = matcher.match(candidate, job);
        assert res.getTotalSkillPoints() == 19 : "Total points should be 19";
        assert res.getEarnedSkillPoints() == 12 : "Earned points should be 12";
        assert Math.abs(res.getSkillScore() - 63.16) < 0.05 : "Skill score should be ~63.16%, got: " + res.getSkillScore();
        assert res.getMatchingRequiredSkills().size() == 2 : "Matching required skills count = 2";
        assert res.getMatchingPreferredSkills().contains("Git") : "Matching preferred skills should contain Git";
        assert res.getMissingPreferredSkills().contains("Docker") : "Missing preferred skills should contain Docker";
    }

    private void testPriorityQueueRankingOrder() {
        Candidate candidate = new Candidate("C5", "Evan", "evan@test.com", "B.Tech", 2.0, "Dev", "Hyderabad",
                Set.of("Java", "SQL", "Spring Boot", "Git"));

        Job jobHighMatch = new Job("J10", "Java Dev", "TechA", "Hyderabad", 2.0, 600000, 900000,
                Set.of("Java", "SQL", "Spring Boot"), Set.of("Git"), "Full Time", "Desc"); // 100% skill match

        Job jobMediumMatch = new Job("J20", "Java Jr", "TechB", "Hyderabad", 2.0, 500000, 700000,
                Set.of("Java", "SQL", "Spring Boot", "Docker"), Set.of(), "Full Time", "Desc"); // 75% skill match

        Job jobLowMatch = new Job("J30", "Python Dev", "TechC", "Hyderabad", 2.0, 400000, 600000,
                Set.of("Python", "Django"), Set.of(), "Full Time", "Desc"); // 0% skill match

        List<Job> jobs = List.of(jobLowMatch, jobHighMatch, jobMediumMatch);
        List<MatchResult> topJobs = matcher.getTopMatchingJobs(candidate, jobs, 3);

        assert topJobs.size() == 3 : "Should return 3 ranked jobs";
        assert topJobs.get(0).getJob().getId().equals("J10") : "Rank #1 should be J10 (highest match)";
        assert topJobs.get(1).getJob().getId().equals("J20") : "Rank #2 should be J20 (medium match)";
        assert topJobs.get(2).getJob().getId().equals("J30") : "Rank #3 should be J30 (low match)";
        assert topJobs.get(0).getMatchScore() >= topJobs.get(1).getMatchScore() : "PriorityQueue ordering check";
    }

    private void testSkillGapPrioritization() {
        Candidate candidate = new Candidate("C6", "Frank", "frank@test.com", "B.Tech", 1.0, "Dev", "Hyderabad",
                Set.of("Java"));
        Job job = new Job("J40", "Dev", "TechCorp", "Hyderabad", 1.0, 600000, 900000,
                Set.of("Java", "Spring Boot", "REST API"), Set.of("Docker", "AWS"), "Full Time", "Desc");

        MatchResult res = matcher.match(candidate, job);
        List<String> gaps = res.getPrioritizedSkillGaps();

        assert gaps.size() == 4 : "Total missing skills count should be 4";
        // Missing required skills MUST come before missing preferred skills
        assert gaps.get(0).equals("Spring Boot") || gaps.get(0).equals("REST API") : "First gap must be a required skill";
        assert gaps.get(1).equals("Spring Boot") || gaps.get(1).equals("REST API") : "Second gap must be a required skill";
        assert gaps.get(2).equals("Docker") || gaps.get(2).equals("AWS") : "Third gap must be a preferred skill";
        assert gaps.get(3).equals("Docker") || gaps.get(3).equals("AWS") : "Fourth gap must be a preferred skill";
    }
}
