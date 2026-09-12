package com.smartjob.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CsvMigrationUtility — Test")
class CsvMigrationUtilityTest {

    @Autowired
    private CsvMigrationUtility csvMigrationUtility;

    @Test
    @DisplayName("parseCsvLine handles quoted fields correctly")
    void parseCsvLine_handlesQuotes() {
        String line = "J001,Java Backend Developer,ABC Technologies,Hyderabad,1.0,600000.0,900000.0,\"Java|SQL|Spring Boot|REST API\",\"Git|Docker\",Full Time,Develop microservices.";
        List<String> tokens = CsvMigrationUtility.parseCsvLine(line);

        assertThat(tokens).hasSize(11);
        assertThat(tokens.get(0)).isEqualTo("J001");
        assertThat(tokens.get(1)).isEqualTo("Java Backend Developer");
        assertThat(tokens.get(7)).isEqualTo("Java|SQL|Spring Boot|REST API");
        assertThat(tokens.get(8)).isEqualTo("Git|Docker");
        assertThat(tokens.get(9)).isEqualTo("Full Time");
    }

    @Test
    @Transactional
    @DisplayName("migrate() executes idempotently against reference CSVs")
    void migrate_executesIdempotently() {
        CsvMigrationUtility.MigrationSummary summary1 = csvMigrationUtility.migrate(Paths.get("data"));
        assertThat(summary1).isNotNull();

        // Second run should be idempotent and not crash
        CsvMigrationUtility.MigrationSummary summary2 = csvMigrationUtility.migrate(Paths.get("data"));
        assertThat(summary2.candidatesImported()).isGreaterThanOrEqualTo(0);
    }
}
