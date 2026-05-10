package com.example.campusrunbackend.dto;

import com.example.campusrunbackend.model.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long id;
    private Long serviceId;
    private String serviceTitle;
    private Long clientId;
    private String clientName;
    private Long providerId;
    private String providerName;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private boolean clientConfirmedDone;
    private boolean providerConfirmedDone;
}
