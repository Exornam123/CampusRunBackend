package com.example.campusrunbackend.dto;

import com.example.campusrunbackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String name;
    private String username;
    private String indexNumber;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String homeAddress;
    private Role role;
    private String providerStatus;
    private String subscriptionStatus;
    private String subscriptionExpiry;
    private String createdAt;
    private boolean online;
    private int jobsCompletedToday;
    private int totalJobsCompleted;
    private String language;
    private Double averageRating;
    private Integer totalReviews;
}
