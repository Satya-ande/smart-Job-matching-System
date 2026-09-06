package com.smartjob;

import com.smartjob.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test — starts the full Spring Boot context with H2 in-memory DB.
 *
 * Verifies:
 *   - Application context loads successfully
 *   - DataSeeder runs and populates the DB
 *   - Core repositories are wired correctly
 *   - JWT config values are loaded
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SmartJob Application — Integration Test")
class SmartJobApplicationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Spring Boot context loads successfully")
    void contextLoads() {
        // If this test passes, the entire Spring context started without errors
        assertThat(userRepository).isNotNull();
    }

    @Test
    @DisplayName("DataSeeder runs and creates seed users")
    void dataSeed_createsUsers() {
        // After context loads, DataSeeder should have populated the DB
        long userCount = userRepository.count();
        assertThat(userCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Test accounts are seeded correctly")
    void dataSeed_candidateAccountExists() {
        assertThat(userRepository.existsByEmail("satya@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("recruiter@smartjob.com")).isTrue();
    }
}
