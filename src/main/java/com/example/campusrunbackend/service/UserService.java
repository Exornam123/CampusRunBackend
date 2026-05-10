package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.ChatMessageRepository;
import com.example.campusrunbackend.repository.ReportRepository;
import com.example.campusrunbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportRepository reportRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       ReportRepository reportRepository, ChatMessageRepository chatMessageRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportRepository = reportRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public User registerUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Providers start as PENDING. Other roles ignore this field.
        if (user.getRole() == com.example.campusrunbackend.model.Role.PROVIDER) {
            user.setProviderStatus(com.example.campusrunbackend.model.ProviderStatus.PENDING);
        }

        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByIndexNumber(String indexNumber) {
        return userRepository.findByIndexNumber(indexNumber);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Manual cleanup for non-cascaded relationships
        reportRepository.deleteAll(reportRepository.findByReporter(user));
        reportRepository.deleteAll(reportRepository.findByReported(user));
        chatMessageRepository.deleteAll(chatMessageRepository.findBySenderId(id));
        chatMessageRepository.deleteAll(chatMessageRepository.findByReceiverId(id));

        userRepository.delete(user);
    }

    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
