package com.example.campusrunbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingAnalyticsDto {
    private String serviceTitle;
    private Integer month;
    private Long count;
}
