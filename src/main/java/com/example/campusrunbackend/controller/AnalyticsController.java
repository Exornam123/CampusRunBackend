package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.BookingAnalyticsDto;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.BookingService;
import com.example.campusrunbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('PROVIDER')")
public class AnalyticsController {

    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public AnalyticsController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingAnalyticsDto>>> getBookingAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUserByIndexNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> data = bookingService.getBookingAnalytics(user.getId());

        List<BookingAnalyticsDto> dtos = data.stream()
                .map(row -> {
                    String title = (String) row[0];
                    Number monthNum = (Number) row[1];
                    Number countNum = (Number) row[2];

                    return new BookingAnalyticsDto(
                            title,
                            monthNum != null ? monthNum.intValue() : 0,
                            countNum != null ? countNum.longValue() : 0L
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "Analytics retrieved"));
    }
}
