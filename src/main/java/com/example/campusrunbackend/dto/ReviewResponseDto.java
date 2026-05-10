package com.example.campusrunbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Long bookingId;
    private String reviewerName;
    private String revieweeName;
    private String providerName;
    private Integer rating;
    private String comment;
    private String serviceTitle;
    private String createdAt;
    private String reviewerRole;
    private String revieweeRole;
}
