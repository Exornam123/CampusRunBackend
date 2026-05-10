package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.ReviewResponseDto;
import com.example.campusrunbackend.model.Review;
import com.example.campusrunbackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportsController {

    private final ReviewService reviewService;

    @Autowired
    public AdminReportsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews/clients")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getClientReviews() {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByReviewerRole("CLIENT")
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(reviews, "Client reviews retrieved"));
    }

    @GetMapping("/reviews/providers")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getProviderReviews() {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByReviewerRole("PROVIDER")
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(reviews, "Provider reviews retrieved"));
    }

    private ReviewResponseDto mapToResponseDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getBooking().getId(),
                review.getReviewer().getName(),
                review.getReviewee().getName(),
                review.getBooking().getService().getProvider().getName(),
                review.getRating(),
                review.getComment(),
                review.getBooking().getService().getTitle(),
                review.getCreatedAt() != null ? review.getCreatedAt().toString() : null,
                review.getReviewer().getRole().name(),
                review.getReviewee().getRole().name()
        );
    }
}
