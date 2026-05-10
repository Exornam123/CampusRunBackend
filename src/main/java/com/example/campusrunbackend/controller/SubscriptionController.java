package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.model.SubscriptionPlan;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.ProviderService;
import com.example.campusrunbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@PreAuthorize("hasRole('PROVIDER')")
public class SubscriptionController {

    private final ProviderService providerService;
    private final UserService userService;

    @Autowired
    public SubscriptionController(ProviderService providerService, UserService userService) {
        this.providerService = providerService;
        this.userService = userService;
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activateSubscription(
            @RequestParam("plan") SubscriptionPlan plan,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUserByIndexNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        providerService.activateSubscription(user, plan);

        return ResponseEntity.ok(ApiResponse.success(null, "Subscription activated successfully"));
    }
}
