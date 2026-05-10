package com.example.campusrunbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderReviewsResponseDto {
    private Long providerId;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponseDto> reviews;
}
