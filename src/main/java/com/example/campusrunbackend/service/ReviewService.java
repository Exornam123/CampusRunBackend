package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.Review;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsByReviewer(User reviewer) {
        return reviewRepository.findByReviewer(reviewer);
    }

    public List<Review> getReviewsByProvider(User provider) {
        return reviewRepository.findByBooking_Service_Provider(provider);
    }

    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }

    public Double calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public boolean existsByBookingAndReviewer(com.example.campusrunbackend.model.Booking booking, User reviewer) {
        return reviewRepository.existsByBookingAndReviewer(booking, reviewer);
    }

    public List<Review> getReviewsByReviewerRole(String role) {
        return reviewRepository.findByReviewer_Role(role);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
