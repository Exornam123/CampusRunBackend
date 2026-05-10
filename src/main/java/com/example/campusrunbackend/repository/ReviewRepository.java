package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.Review;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking", "booking.service", "booking.service.provider"})
    List<Review> findByReviewer(User reviewer);

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking", "booking.service", "booking.service.provider"})
    List<Review> findByBooking_Service_Provider(User provider);

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking", "booking.service", "booking.service.provider"})
    List<Review> findByBooking_Service_ProviderIn(List<User> providers);

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking", "booking.service", "booking.service.provider"})
    List<Review> findByReviewee(User reviewee);

    boolean existsByBookingAndReviewer(com.example.campusrunbackend.model.Booking booking, User reviewer);

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking", "booking.service", "booking.service.provider"})
    List<Review> findByReviewer_Role(String role);

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE reviews DROP CONSTRAINT IF EXISTS reviews_booking_id_key", nativeQuery = true)
    void dropReviewBookingConstraint();
}
