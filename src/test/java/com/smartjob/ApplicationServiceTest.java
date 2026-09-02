package com.smartjob;

import com.smartjob.model.Application;
import com.smartjob.model.ApplicationStatus;
import com.smartjob.service.ApplicationService;

import java.util.List;

/**
 * Unit tests for ApplicationService and duplicate application prevention.
 */
public class ApplicationServiceTest {

    public void runAllTests() {
        System.out.println("Running ApplicationServiceTest...");
        testApplySuccess();
        testDuplicateApplicationPrevention();
        testGetApplicationsByCandidate();
        testUpdateApplicationStatus();
        System.out.println("  [PASS] All ApplicationServiceTest cases passed successfully.\n");
    }

    private void testApplySuccess() {
        ApplicationService service = new ApplicationService();
        Application app = service.apply("C001", "J001");

        assert app != null : "Application should be created";
        assert app.getId().equals("A001") : "First application ID should be A001";
        assert app.getCandidateId().equals("C001") : "Candidate ID should match";
        assert app.getJobId().equals("J001") : "Job ID should match";
        assert app.getStatus() == ApplicationStatus.APPLIED : "Initial status should be APPLIED";
    }

    private void testDuplicateApplicationPrevention() {
        ApplicationService service = new ApplicationService();
        service.apply("C001", "J001");

        boolean threwException = false;
        try {
            service.apply("C001", "J001"); // Second attempt for same candidate and job
        } catch (IllegalStateException e) {
            threwException = true;
            assert e.getMessage().contains("already applied") : "Exception message should indicate duplicate application";
        }

        assert threwException : "Duplicate application attempt must throw IllegalStateException";
        assert service.getAllApplications().size() == 1 : "Only 1 application should be stored in system";
    }

    private void testGetApplicationsByCandidate() {
        ApplicationService service = new ApplicationService();
        service.apply("C001", "J001");
        service.apply("C001", "J002");
        service.apply("C002", "J001");

        List<Application> c1Apps = service.getApplicationsByCandidate("C001");
        assert c1Apps.size() == 2 : "Candidate C001 should have 2 applications";

        List<Application> c2Apps = service.getApplicationsByCandidate("C002");
        assert c2Apps.size() == 1 : "Candidate C002 should have 1 application";
    }

    private void testUpdateApplicationStatus() {
        ApplicationService service = new ApplicationService();
        Application app = service.apply("C001", "J001");

        boolean updated = service.updateStatus(app.getId(), ApplicationStatus.SHORTLISTED);
        assert updated : "Status update should succeed";
        assert app.getStatus() == ApplicationStatus.SHORTLISTED : "Application status should now be SHORTLISTED";

        service.updateStatus(app.getId(), ApplicationStatus.SELECTED);
        assert app.getStatus() == ApplicationStatus.SELECTED : "Application status should now be SELECTED";
    }
}
