package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.ProviderStatus;
import com.example.campusrunbackend.model.Report;
import com.example.campusrunbackend.model.Role;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.ReportRepository;
import com.example.campusrunbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.campusrunbackend.dto.ProviderAdminDetailDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final com.example.campusrunbackend.repository.ProviderProfileRepository providerProfileRepository;
    private final UserService userService;
    private final ProviderService providerService;

    @Autowired
    public AdminService(UserRepository userRepository, ReportRepository reportRepository,
                        com.example.campusrunbackend.repository.ProviderProfileRepository providerProfileRepository,
                        UserService userService, ProviderService providerService) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.userService = userService;
        this.providerService = providerService;
    }


    // 1. Fetch and Approve/Reject pending providers
    @Transactional(readOnly = true)
    public List<ProviderAdminDetailDto> getPendingProviders() {
        return userRepository.findAllByRoleAndProviderStatus(Role.PROVIDER, ProviderStatus.PENDING)
                .stream()
                .map(providerService::mapUserToAdminDetailDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProviderAdminDetailDto> getApprovedProviders() {
        return userRepository.findAllByRoleAndProviderStatus(Role.PROVIDER, ProviderStatus.APPROVED)
                .stream()
                .map(providerService::mapUserToAdminDetailDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProviderAdminDetailDto> getRejectedProviders() {
        return userRepository.findAllByRoleAndProviderStatus(Role.PROVIDER, ProviderStatus.REJECTED)
                .stream()
                .map(providerService::mapUserToAdminDetailDto)
                .collect(Collectors.toList());
    }




    public User approveProvider(Long userId) {
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.PROVIDER) {
            throw new RuntimeException("User is not a Provider");
        }
        user.setProviderStatus(ProviderStatus.APPROVED);
        return userRepository.save(user);
    }

    public void rejectProvider(Long userId) {
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.PROVIDER) {
            throw new RuntimeException("User is not a Provider");
        }
        user.setProviderStatus(ProviderStatus.REJECTED);
        userRepository.save(user);
    }

    // 2. Manage all users (view/delete)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }

    // 3. Handle/view user reports
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report resolveReport(Long reportId, String adminComment) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setResolved(true);
        report.setAdminComment(adminComment);
        return reportRepository.save(report);
    }
}
