package com.smartjob;

import com.smartjob.model.Candidate;
import com.smartjob.service.CandidateService;

import java.util.Set;

/**
 * Unit tests for CandidateService and skill set management.
 */
public class CandidateServiceTest {

    public void runAllTests() {
        System.out.println("Running CandidateServiceTest...");
        testCreateCandidateAndIdGeneration();
        testAddSkillNew();
        testAddDuplicateSkill();
        testRemoveExistingSkill();
        testRemoveMissingSkill();
        testUpdateCandidate();
        System.out.println("  [PASS] All CandidateServiceTest cases passed successfully.\n");
    }

    private void testCreateCandidateAndIdGeneration() {
        CandidateService service = new CandidateService();
        Candidate c1 = new Candidate(null, "John Doe", "john@example.com", "B.Tech", 2.0, "Java Developer", "Hyderabad");
        service.addCandidate(c1);

        assert c1.getId() != null : "Candidate ID must be auto-generated";
        assert c1.getId().equals("C001") : "First candidate ID should be C001, got: " + c1.getId();

        Candidate c2 = new Candidate(null, "Jane Doe", "jane@example.com", "M.Tech", 4.0, "Tech Lead", "Bengaluru");
        service.addCandidate(c2);
        assert c2.getId().equals("C002") : "Second candidate ID should be C002, got: " + c2.getId();
    }

    private void testAddSkillNew() {
        Candidate candidate = new Candidate("C10", "Sam", "sam@example.com", "B.Sc", 1.0, "Dev", "Pune");
        boolean added = candidate.addSkill("Java");
        assert added : "Adding new skill should return true";
        assert candidate.getSkills().contains("Java") : "Candidate skill set should contain Java";
        assert candidate.hasSkill("Java") : "hasSkill should return true";
    }

    private void testAddDuplicateSkill() {
        Candidate candidate = new Candidate("C11", "Sam", "sam@example.com", "B.Sc", 1.0, "Dev", "Pune",
                Set.of("Java", "SQL"));

        // Case-insensitive duplicate test
        boolean addedSameCase = candidate.addSkill("Java");
        assert !addedSameCase : "Adding exact duplicate skill 'Java' should return false";

        boolean addedLowerCase = candidate.addSkill("java");
        assert !addedLowerCase : "Adding case-variant duplicate 'java' should return false";

        boolean addedUpperCase = candidate.addSkill("JAVA");
        assert !addedUpperCase : "Adding case-variant duplicate 'JAVA' should return false";

        assert candidate.getSkills().size() == 2 : "Candidate skill set size should remain 2";
    }

    private void testRemoveExistingSkill() {
        Candidate candidate = new Candidate("C12", "Sam", "sam@example.com", "B.Sc", 1.0, "Dev", "Pune",
                Set.of("Java", "Python", "SQL"));

        boolean removed = candidate.removeSkill("python"); // case-insensitive removal
        assert removed : "Removing existing skill should return true";
        assert !candidate.hasSkill("Python") : "Candidate should no longer have Python";
        assert candidate.getSkills().size() == 2 : "Skill count should be 2";
    }

    private void testRemoveMissingSkill() {
        Candidate candidate = new Candidate("C13", "Sam", "sam@example.com", "B.Sc", 1.0, "Dev", "Pune",
                Set.of("Java", "SQL"));

        boolean removed = candidate.removeSkill("Kubernetes");
        assert !removed : "Removing non-existent skill should return false";
        assert candidate.getSkills().size() == 2 : "Skill count should remain 2";
    }

    private void testUpdateCandidate() {
        CandidateService service = new CandidateService();
        Candidate c = new Candidate("C001", "Old Name", "old@test.com", "B.Tech", 1.0, "Junior Dev", "Pune");
        service.addCandidate(c);

        c.setName("New Name");
        c.setExperience(3.0);
        c.setPreferredLocation("Hyderabad");

        boolean updated = service.updateCandidate(c);
        assert updated : "Update candidate should return true";

        Candidate fetched = service.getCandidateById("C001");
        assert fetched.getName().equals("New Name") : "Candidate name should be updated";
        assert fetched.getExperience() == 3.0 : "Candidate experience should be updated";
        assert fetched.getPreferredLocation().equals("Hyderabad") : "Location should be updated";
    }
}
