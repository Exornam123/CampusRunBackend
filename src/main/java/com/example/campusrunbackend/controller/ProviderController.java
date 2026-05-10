package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.ProviderRegistrationDto;
import com.example.campusrunbackend.dto.ProviderResponseDto;
import com.example.campusrunbackend.model.ProviderProfile;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.ProviderService;
import com.example.campusrunbackend.service.UserService;
import com.example.campusrunbackend.dto.ProviderDashboardDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;
    private final UserService userService;

    @Autowired
    public ProviderController(ProviderService providerService, UserService userService) {
        this.providerService = providerService;
        this.userService = userService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> registerProvider(
            @Valid @RequestPart("registration") ProviderRegistrationDto dto,
            @RequestPart(value = "cv", required = false) List<MultipartFile> cvs,
            @RequestPart(value = "proof", required = false) List<MultipartFile> proofs,
            @AuthenticationPrincipal UserDetails currentUser) throws Exception {

        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        ProviderProfile profile = providerService.registerProvider(user, dto, cvs, proofs);
        ProviderResponseDto data = providerService.mapToResponseDto(profile);

        return ResponseEntity.ok(ApiResponse.success(data, "Provider registration successful"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> getProviderById(@PathVariable Long id) {
        ProviderResponseDto data = providerService.getProviderById(id);
        return ResponseEntity.ok(ApiResponse.success(data, "Provider profile retrieved"));
    }

    @GetMapping("/discovery")
    public ResponseEntity<ApiResponse<List<com.example.campusrunbackend.dto.ProviderDiscoveryDto>>> discoverProviders(@RequestParam("category_id") Long categoryId) {
        List<com.example.campusrunbackend.dto.ProviderDiscoveryDto> data = providerService.discoverProviders(categoryId);
        return ResponseEntity.ok(ApiResponse.success(data, "Providers retrieved successfully"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<ProviderDashboardDto>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByIndexNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(providerService.getDashboardData(user), "Dashboard retrieved"));
    }

    @PutMapping("/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByIndexNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        providerService.toggleOnlineStatus(user);
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated"));
    }
}
