package com.smartjob;

import com.smartjob.util.InputValidator;

/**
 * Unit tests for InputValidator.
 */
public class InputValidatorTest {

    public void runAllTests() {
        System.out.println("Running InputValidatorTest...");
        testEmailValidation();
        testExperienceValidation();
        testSalaryValidation();
        testStringValidation();
        System.out.println("  [PASS] All InputValidatorTest cases passed successfully.\n");
    }

    private void testEmailValidation() {
        assert InputValidator.isValidEmail("satya@example.com") : "Valid email should pass";
        assert InputValidator.isValidEmail("john.doe+work@domain.co.in") : "Valid complex email should pass";
        assert !InputValidator.isValidEmail("invalid-email") : "Missing @ and domain should fail";
        assert !InputValidator.isValidEmail("user@.com") : "Invalid domain should fail";
        assert !InputValidator.isValidEmail("") : "Empty email should fail";
        assert !InputValidator.isValidEmail(null) : "Null email should fail";
    }

    private void testExperienceValidation() {
        assert InputValidator.isValidExperience(0.0) : "Fresher (0.0 years) is valid";
        assert InputValidator.isValidExperience(3.5) : "3.5 years is valid";
        assert !InputValidator.isValidExperience(-1.0) : "Negative experience must be invalid";
        assert !InputValidator.isValidExperience(-0.1) : "Negative experience must be invalid";
        assert !InputValidator.isValidExperience(70.0) : "Excessive experience > 60 years should be invalid";
    }

    private void testSalaryValidation() {
        assert InputValidator.isValidSalary(600000.0) : "Positive salary is valid";
        assert InputValidator.isValidSalary(0.0) : "0 salary is valid";
        assert !InputValidator.isValidSalary(-5000.0) : "Negative salary must be invalid";

        assert InputValidator.isValidSalaryRange(500000.0, 800000.0) : "Valid range where min <= max";
        assert !InputValidator.isValidSalaryRange(800000.0, 500000.0) : "Invalid range where min > max";
    }

    private void testStringValidation() {
        assert InputValidator.isNotEmpty("Java") : "Non-empty string is valid";
        assert !InputValidator.isNotEmpty("") : "Empty string is invalid";
        assert !InputValidator.isNotEmpty("   ") : "Whitespace-only string is invalid";
        assert !InputValidator.isNotEmpty(null) : "Null string is invalid";
    }
}
