package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.UserProfileDto;
import com.example.campusrunbackend.dto.UserUpdateDto;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import com.example.campusrunbackend.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final com.example.campusrunbackend.repository.ReviewRepository reviewRepository;

    @Autowired
    public UserController(UserService userService, FileStorageService fileStorageService, com.example.campusrunbackend.repository.ReviewRepository reviewRepository) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToProfileDto(user), "Current user profile retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwnProfile = currentUser.getUsername().equals(user.getIndexNumber());

        UserProfileDto dto = mapToProfileDto(user);

        // Filter sensitive data for others
        if (!isAdmin && !isOwnProfile) {
            dto.setEmail(null);
            dto.setPhoneNumber(null);
        }

        return ResponseEntity.ok(ApiResponse.success(dto, "User profile retrieved"));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @Valid @RequestBody UserUpdateDto updateDto,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found"));

        user.setName(updateDto.getName());
        user.setUsername(updateDto.getUsername());
        user.setEmail(updateDto.getEmail());
        user.setPhoneNumber(updateDto.getPhoneNumber());
        user.setHomeAddress(updateDto.getHomeAddress());
        user.setLanguage(updateDto.getLanguage());

        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(ApiResponse.success(mapToProfileDto(updatedUser), "Profile updated successfully"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody com.example.campusrunbackend.dto.ChangePasswordDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        try {
            userService.changePassword(user, dto.getOldPassword(), dto.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<ApiResponse<UserProfileDto>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String storedPath = fileStorageService.storeFile(file, "profiles");
        user.setProfilePicture(storedPath);

        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(ApiResponse.success(mapToProfileDto(updatedUser), "Profile picture updated"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userService.deleteUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserProfileDto> dtos = users.stream()
                .map(user -> mapToProfileDto(user))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "All users retrieved"));
    }

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
}
