package com.example.campusrunbackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class CampusRunBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusRunBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner dropConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check");
                System.out.println("Successfully dropped bookings_status_check constraint if it existed.");
            } catch (Exception e) {
                System.out.println("Failed to drop constraint: " + e.getMessage());
            }
        };
    }
}
