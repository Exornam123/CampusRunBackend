package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.BookingCreateDto;
import com.example.campusrunbackend.dto.BookingResponseDto;
import com.example.campusrunbackend.dto.BookingStatusUpdateDto;
import com.example.campusrunbackend.model.Booking;
import com.example.campusrunbackend.model.BookingStatus;
import com.example.campusrunbackend.model.CampusService;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.BookingService;
import com.example.campusrunbackend.service.CampusServiceService;
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
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CampusServiceService serviceService;
    private final UserService userService;

    @Autowired
    public BookingController(BookingService bookingService,
                             CampusServiceService serviceService,
                             UserService userService) {
        this.bookingService = bookingService;
        this.serviceService = serviceService;
        this.userService = userService;
    }

    // ── CLIENT: Create a booking ─────────────────────────────────────────────

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @Valid @RequestBody BookingCreateDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {

        User client = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        CampusService service = serviceService.getServiceById(dto.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        Booking booking = new Booking();
        booking.setClient(client);
        booking.setService(service);
        booking.setStatus(BookingStatus.NEW);
        booking.setBookingDate(LocalDateTime.now());

        Booking savedBooking = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapToResponseDto(savedBooking), "Booking created successfully"));
    }

    // ── CLIENT: Get my bookings (no user ID in path — uses JWT identity) ─────

    @GetMapping("/my/client")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getMyClientBookings(
            @AuthenticationPrincipal UserDetails currentUser) {

        User client = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<BookingResponseDto> data = bookingService.getBookingsByClient(client)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(data, "Your client bookings retrieved"));
    }

    // ── PROVIDER: Get my incoming bookings (no user ID in path) ─────────────

    @GetMapping("/my/provider")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getMyProviderBookings(
            @AuthenticationPrincipal UserDetails currentUser) {

        User provider = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<BookingResponseDto> data = bookingService.getBookingsByProvider(provider)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(data, "Your provider bookings retrieved"));
    }

    // ── Legacy: Get bookings by client user ID (ADMIN or own access) ─────────

    @GetMapping("/client/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getBookingsForClient(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {

        User client = userService.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateAccess(client, currentUser);

        List<BookingResponseDto> data = bookingService.getBookingsByClient(client)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(data, "Client bookings retrieved"));
    }

    // ── Legacy: Get bookings by provider user ID (ADMIN or own access) ────────

    @GetMapping("/provider/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getBookingsForProvider(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {

        User provider = userService.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateAccess(provider, currentUser);

        List<BookingResponseDto> data = bookingService.getBookingsByProvider(provider)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(data, "Provider bookings retrieved"));
    }

    // ── PROVIDER/ADMIN: Update booking status ────────────────────────────────

    // ── PROVIDER/ADMIN: Update booking status ────────────────────────────────

    @PutMapping("/update-status")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateStatus(
            @RequestBody BookingStatusUpdateDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {

        Booking booking = bookingService.getBookingById(dto.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getService().getProvider().getIndexNumber().equals(currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized to update this booking's status");
        }

        if (dto.getStatus() == BookingStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Directly setting status to DELIVERED is not allowed. Use /{id}/confirm-done instead.");
        }

        booking.setStatus(dto.getStatus());
        Booking updatedBooking = bookingService.updateBooking(booking);

        // DELIVERED logic moved to confirmDone, but keep basic update here
        return ResponseEntity.ok(ApiResponse.success(mapToResponseDto(updatedBooking), "Status updated successfully"));
    }

    @PutMapping("/{id}/confirm-done")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> confirmDone(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        User user = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isClient = booking.getClient().getId().equals(user.getId());
        boolean isProvider = booking.getService().getProvider().getId().equals(user.getId());

        if (!isClient && !isProvider) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to confirm this booking");
        }

        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == BookingStatus.COMPLETED) {
            return ResponseEntity.ok(ApiResponse.success(mapToResponseDto(booking), "Booking is already completed!"));
        }

        if (currentStatus == BookingStatus.PROCESSING) {
            if (isProvider) {
                booking.setStatus(BookingStatus.PROVIDER_COMPLETED);
            } else if (isClient) {
                booking.setStatus(BookingStatus.CLIENT_COMPLETED);
            }
        } else if (currentStatus == BookingStatus.PROVIDER_COMPLETED) {
            if (isClient) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (isProvider) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already marked this booking as completed");
            }
        } else if (currentStatus == BookingStatus.CLIENT_COMPLETED) {
            if (isProvider) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (isClient) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already marked this booking as completed");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status for completion");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            // Update provider stats
            User provider = booking.getService().getProvider();
            provider.setJobsCompletedToday(provider.getJobsCompletedToday() + 1);
            provider.setTotalJobsCompleted(provider.getTotalJobsCompleted() + 1);
            userService.updateUser(provider);
        }

        Booking updatedBooking = bookingService.updateBooking(booking);

        String msg;
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            msg = "Booking marked as fully completed!";
        } else if (booking.getStatus() == BookingStatus.PROVIDER_COMPLETED) {
            msg = "Provider marked as completed. Waiting for client confirmation.";
        } else {
            msg = "Client marked as completed. Waiting for provider confirmation.";
        }

        return ResponseEntity.ok(ApiResponse.success(mapToResponseDto(updatedBooking), msg));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateAccess(User targetUser, UserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !currentUser.getUsername().equals(targetUser.getIndexNumber())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to other user's bookings");
        }
    }

    private BookingResponseDto mapToResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getService().getId(),
                booking.getService().getTitle(),
                booking.getClient().getId(),
                booking.getClient().getName(),
                booking.getService().getProvider().getId(),
                booking.getService().getProvider().getName(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.isClientConfirmedDone(),
                booking.isProviderConfirmedDone()
        );
    }
}
