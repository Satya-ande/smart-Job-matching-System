package com.smartjob;

/**
 * Main entry point for executing all unit test suites.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                      SMARTJOB AUTOMATED TEST SUITE                             ");
        System.out.println("================================================================================\n");

        int passed = 0;
        int failed = 0;

        // 1. JobMatcherTest
        try {
            new JobMatcherTest().runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] JobMatcherTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 2. CandidateServiceTest
        try {
            new CandidateServiceTest().runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] CandidateServiceTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 3. ApplicationServiceTest
        try {
            new ApplicationServiceTest().runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] ApplicationServiceTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 4. InputValidatorTest
        try {
            new InputValidatorTest().runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] InputValidatorTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("================================================================================");
        System.out.printf("TEST SUMMARY: %d Suite(s) Passed, %d Suite(s) Failed%n", passed, failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
