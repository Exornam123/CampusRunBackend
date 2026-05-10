package com.example.campusrunbackend.config;

import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.repository.ServiceCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initCategories(ServiceCategoryRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<String> categories = Arrays.asList(
                        "Errand Services",
                        "Skill-Based Services",
                        "Tech Support",
                        "Home Support",
                        "Academic Support",
                        "Custom Support"
                );

                for (String name : categories) {
                    ServiceCategory category = new ServiceCategory();
                    category.setName(name);
                    category.setDescription("Standard category for " + name);
                    repository.save(category);
                }
                System.out.println("Service categories seeded successfully.");
            }
        };
    }
}
