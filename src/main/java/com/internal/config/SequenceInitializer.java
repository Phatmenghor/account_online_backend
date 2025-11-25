package com.internal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SequenceInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and resetting sequences if necessary...");
        try {
            // Reset sequence for location_province_cbc
            // This assumes PostgreSQL. If using another DB, syntax might differ.
            // The error code 23505 suggests PostgreSQL or similar.
            String sql = "SELECT setval('location_province_cbc_id_seq', (SELECT MAX(id) FROM location_province_cbc));";
            jdbcTemplate.execute(sql);
            log.info("Successfully reset sequence for location_province_cbc");
        } catch (Exception e) {
            log.error("Failed to reset sequence: {}", e.getMessage());
            // Don't fail startup, just log error
        }
    }
}
