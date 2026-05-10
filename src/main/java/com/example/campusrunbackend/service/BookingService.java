package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.Booking;
import com.example.campusrunbackend.model.CampusService;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByClient(User client) {
        return bookingRepository.findByClient(client);
    }

    public List<Booking> getBookingsByService(CampusService service) {
        return bookingRepository.findByService(service);
    }

    public List<Booking> getBookingsByProvider(User provider) {
        return bookingRepository.findByService_Provider(provider);
    }

    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public List<Object[]> getBookingAnalytics(Long providerId) {
        return bookingRepository.getBookingAnalytics(providerId);
    }
}
