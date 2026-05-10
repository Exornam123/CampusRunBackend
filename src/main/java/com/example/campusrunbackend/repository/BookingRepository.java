package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.Booking;
import com.example.campusrunbackend.model.CampusService;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"client", "service", "service.provider", "service.category"})
    List<Booking> findByClient(User client);

    @EntityGraph(attributePaths = {"client", "service", "service.provider", "service.category"})
    List<Booking> findByService(CampusService service);

    @EntityGraph(attributePaths = {"client", "service", "service.provider", "service.category"})
    List<Booking> findByService_Provider(User provider);

    @Override
    @EntityGraph(attributePaths = {"client", "service", "service.provider", "service.category"})
    Optional<Booking> findById(Long id);

    long countByService_ProviderAndStatus(User provider, com.example.campusrunbackend.model.BookingStatus status);

    List<Booking> findByService_ProviderAndStatus(User provider, com.example.campusrunbackend.model.BookingStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT s.title, EXTRACT(MONTH FROM b.booking_date) as month, COUNT(b.id) " +
            "FROM bookings b " +
            "JOIN services s ON b.service_id = s.id " +
            "WHERE s.provider_id = :providerId " +
            "GROUP BY s.title, month " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> getBookingAnalytics(@org.springframework.data.repository.query.Param("providerId") Long providerId);
}
