package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.ProviderReviewsResponseDto;
import com.example.campusrunbackend.dto.ReviewCreateDto;
import com.example.campusrunbackend.dto.ReviewResponseDto;
import com.example.campusrunbackend.model.Booking;
import com.example.campusrunbackend.model.BookingStatus;
import com.example.campusrunbackend.model.Review;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.BookingService;
import com.example.campusrunbackend.service.ReviewService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public ReviewController(ReviewService reviewService,
                            BookingService bookingService,
                            UserService userService) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @Valid @RequestBody ReviewCreateDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {

        Booking booking = bookingService.getBookingById(dto.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviews are only allowed for completed bookings");
        }

        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isClient = booking.getClient().getId().equals(user.getId());
        boolean isProvider = booking.getService().getProvider().getId().equals(user.getId());

        if (!isClient && !isProvider) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only review your own bookings");
        }

        User reviewee = isClient ? booking.getService().getProvider() : booking.getClient();

        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        // Check if this user already reviewed this booking
        boolean alreadyReviewed = reviewService.existsByBookingAndReviewer(booking, user);
        if (alreadyReviewed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(user);
        review.setReviewee(reviewee);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapToResponseDto(savedReview), "Review submitted successfully"));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<ProviderReviewsResponseDto>> getProviderReviews(@PathVariable Long providerId) {
        User provider = userService.getUserById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        List<Review> reviews = reviewService.getReviewsByProvider(provider);
        Double averageRating = reviewService.calculateAverageRating(reviews);

        List<ReviewResponseDto> reviewDtos = reviews.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        ProviderReviewsResponseDto data = new ProviderReviewsResponseDto(
                provider.getId(),
                averageRating,
                reviews.size(),
                reviewDtos
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Provider reviews retrieved"));
    }

    @GetMapping("/client-reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getClientReviewsReport() {
        List<Review> reviews = reviewService.getReviewsByReviewerRole("CLIENT");
        List<ReviewResponseDto> dtos = reviews.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "Client reviews retrieved"));
    }

    @GetMapping("/provider-reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getProviderReviewsReport() {
        List<Review> reviews = reviewService.getReviewsByReviewerRole("PROVIDER");
        List<ReviewResponseDto> dtos = reviews.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "Provider reviews retrieved"));
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
