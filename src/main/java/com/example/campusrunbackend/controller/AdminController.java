package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.ProviderAdminDetailDto;
import com.example.campusrunbackend.dto.UserProfileDto;
import com.example.campusrunbackend.model.Report;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.AdminService;
import com.example.campusrunbackend.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProviderService providerService;
    private final com.example.campusrunbackend.repository.UserRepository userRepository;
    private final com.example.campusrunbackend.repository.ProviderProfileRepository providerProfileRepository;
    private final com.example.campusrunbackend.repository.ServiceCategoryRepository categoryRepository;
    private final com.example.campusrunbackend.repository.ReviewRepository reviewRepository;

    @Autowired
    public AdminController(AdminService adminService, ProviderService providerService,
                           com.example.campusrunbackend.repository.UserRepository userRepository,
                           com.example.campusrunbackend.repository.ProviderProfileRepository providerProfileRepository,
                           com.example.campusrunbackend.repository.ServiceCategoryRepository categoryRepository,
                           com.example.campusrunbackend.repository.ReviewRepository reviewRepository) {
        this.adminService = adminService;
        this.providerService = providerService;
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
    }

    // ── Provider Management ─────────────────────────────────────────────────

    @GetMapping("/providers/pending")
    public ResponseEntity<ApiResponse<List<ProviderAdminDetailDto>>> getPendingProviders() {
        List<ProviderAdminDetailDto> data = adminService.getPendingProviders();
        return ResponseEntity.ok(ApiResponse.success(data, "Pending providers retrieved"));
    }



    @GetMapping("/providers/approved")
    public ResponseEntity<ApiResponse<List<ProviderAdminDetailDto>>> getApprovedProviders() {
        List<ProviderAdminDetailDto> data = adminService.getApprovedProviders();
        return ResponseEntity.ok(ApiResponse.success(data, "Approved providers retrieved"));
    }



    @GetMapping("/providers/rejected")
    public ResponseEntity<ApiResponse<List<ProviderAdminDetailDto>>> getRejectedProviders() {
        List<ProviderAdminDetailDto> data = adminService.getRejectedProviders();
        return ResponseEntity.ok(ApiResponse.success(data, "Rejected providers retrieved"));
    }



    @PutMapping("/providers/approve/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> approveProvider(@PathVariable Long id) {
        User approved = adminService.approveProvider(id);
        return ResponseEntity.ok(ApiResponse.success(mapToProfileDto(approved), "Provider approved"));
    }

    @PutMapping("/providers/reject/{id}")
    public ResponseEntity<ApiResponse<Void>> rejectProvider(@PathVariable Long id) {
        adminService.rejectProvider(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Provider rejected/deleted"));
    }

    // ── User Management ─────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        List<UserProfileDto> data = adminService.getAllUsers()
                .stream()
                .map(this::mapToProfileDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data, "All users retrieved"));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserById(@PathVariable Long id) {
        User user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(mapToProfileDto(user), "User details retrieved"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    // ── Report Management ───────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<Report>>> getAllReports() {
        List<Report> data = adminService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success(data, "Reports retrieved"));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse<Report>> resolveReport(
            @PathVariable Long id,
            @RequestBody String adminComment) {
        Report data = adminService.resolveReport(id, adminComment);
        return ResponseEntity.ok(ApiResponse.success(data, "Report resolved"));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private UserProfileDto mapToProfileDto(User user) {
        List<com.example.campusrunbackend.model.Review> reviews = reviewRepository.findByReviewee(user);
        double avgRating = reviews.stream().mapToInt(com.example.campusrunbackend.model.Review::getRating).average().orElse(0.0);
        int totalReviews = reviews.size();

        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getIndexNumber(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfilePicture(),
                user.getHomeAddress(),
                user.getRole(),
                user.getProviderStatus() != null ? user.getProviderStatus().name() : null,
                user.getSubscriptionStatus(),
                user.getSubscriptionExpiry() != null ? user.getSubscriptionExpiry().toString() : null,
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.isOnline(),
                user.getJobsCompletedToday(),
                user.getTotalJobsCompleted(),
                user.getLanguage(),
                avgRating,
                totalReviews
        );
    }

    @GetMapping("/force-approve-all")
    public ResponseEntity<ApiResponse<Void>> forceApproveAll() {
        List<com.example.campusrunbackend.model.ServiceCategory> allCategories = categoryRepository.findAll();
        List<User> providers = adminService.getAllUsers().stream()
                .filter(u -> u.getRole() == com.example.campusrunbackend.model.Role.PROVIDER)
                .collect(Collectors.toList());

        int fixedCount = 0;
        for (User u : providers) {
            u.setProviderStatus(com.example.campusrunbackend.model.ProviderStatus.APPROVED);
            u.setOnline(true);

            com.example.campusrunbackend.model.ProviderProfile profile = u.getProviderProfile();
            if (profile != null) {
                profile.setCategories(new java.util.HashSet<>(allCategories));
                providerProfileRepository.save(profile);
            }
            userRepository.save(u);
            fixedCount++;
        }
        return ResponseEntity.ok(ApiResponse.success(null, "NUCLEAR FIX: Repaired " + fixedCount + " providers. They are now Approved, Online, and linked to ALL " + allCategories.size() + " categories."));
    }
}
