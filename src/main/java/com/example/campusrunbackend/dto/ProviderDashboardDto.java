package com.example.campusrunbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDashboardDto {
    private String greeting;
    private String statusMessage;
    private boolean online;
    private int currentProgress;
    private int targetGoal;
    private String challengeStatus; // "ACTIVE", "COMPLETED", "BONUS"
    private Double averageRating;
    private Integer totalReviews;
    private int newRequestsCount;
    private int activeJobsCount;
}
