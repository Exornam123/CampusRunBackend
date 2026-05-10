package com.example.campusrunbackend.config;

import com.example.campusrunbackend.model.Role;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByIndexNumber("admin").isEmpty()) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setUsername("CampusRun Admin");
            admin.setIndexNumber("admin"); // used for login
            admin.setEmail("admin@campusrun.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            // Admins are approved by default
            userRepository.save(admin);
            System.out.println("AdminSeeder: Default admin created successfully. Login with 'admin':'admin123'");
        } else {
            System.out.println("AdminSeeder: Admin user already exists. Skipping seeding.");
        }
    }
}
