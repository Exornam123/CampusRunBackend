package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.model.Promotion;
import com.example.campusrunbackend.model.ProviderPromotion;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.PromotionService;
import com.example.campusrunbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final UserService userService;

    @Autowired
    public PromotionController(PromotionService promotionService, UserService userService) {
        this.promotionService = promotionService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<Promotion>>> getAvailablePromotions() {
        List<Promotion> data = promotionService.getAvailablePromotions();
        return ResponseEntity.ok(ApiResponse.success(data, "Available promotions retrieved"));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<ProviderPromotion>>> getActivePromotions(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByIndexNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ProviderPromotion> data = promotionService.getActivePromotions(user);
        return ResponseEntity.ok(ApiResponse.success(data, "Active promotions retrieved"));
    }

    @PostMapping("/activate")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> activatePromoCode(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String code) {
        User user = userService.getUserByIndexNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        promotionService.activatePromoCode(user, code);
        return ResponseEntity.ok(ApiResponse.success(null, "Promo code activated successfully"));
    }
}
