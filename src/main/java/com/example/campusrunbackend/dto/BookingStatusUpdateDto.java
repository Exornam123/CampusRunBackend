package com.example.campusrunbackend.dto;

import com.example.campusrunbackend.model.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdateDto {
    private Long bookingId;
    private BookingStatus status;
}
