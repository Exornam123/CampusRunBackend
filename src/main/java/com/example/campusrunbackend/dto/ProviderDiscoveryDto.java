package com.example.campusrunbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.example.campusrunbackend.model.SubscriptionPlan;
import com.example.campusrunbackend.model.ProviderRank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDiscoveryDto {
    private Long id;
    private String name;
    private String profileImage;
    private List<String> services;
    private boolean isOnline;
    private String providerPhone;
    private Double averageRating;
    private Integer reviewCount;
    private Long providerId;

    // New fields for the UI sync
    private SubscriptionPlan subscriptionPlan;
    private ProviderRank rank;
    private String providerName;
    private String title;
    private Double price;
    private String description;
    private List<ReviewResponseDto> recentReviews;
    private String promotionBadgeLabel;
    private Integer promotionScore; // For sorting: Featured=4, Trending=3, Top Rated=2, New Boost=1, None=0

    // Manual constructor for legacy compatibility in Service
    public ProviderDiscoveryDto(Long id, String name, String profileImage, List<String> services,
                                boolean isOnline, String providerPhone, Double averageRating, Integer reviewCount) {
        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
        this.services = services;
        this.isOnline = isOnline;
        this.providerPhone = providerPhone;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.providerName = name;
    }
}
