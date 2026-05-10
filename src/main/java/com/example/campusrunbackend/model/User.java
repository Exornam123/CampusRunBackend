package com.example.campusrunbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Real name

    @Column(nullable = false, unique = true)
    private String username; // Display name

    @Column(nullable = false, unique = true)
    private String indexNumber; // Authentication ID

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_status", nullable = true)
    private ProviderStatus providerStatus; // Only relevant for providers

    @Column(nullable = true)
    private String profilePicture;

    @Column(nullable = true)
    private String homeAddress;

    @Column(nullable = false, columnDefinition = "varchar(255) default 'FREE'")
    private String subscriptionStatus = "FREE";

    @Column(nullable = true)
    private String qualification;

    @Column(nullable = false, columnDefinition = "varchar(20) default 'English'")
    private String language = "English";

    @Column(nullable = true)
    private java.time.LocalDateTime subscriptionExpiry;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isOnline = false;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int jobsCompletedToday = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int totalJobsCompleted = 0;

    @Column(nullable = true)
    private java.time.LocalDateTime lastProgressReset;

    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProviderProfile providerProfile;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampusService> providedServices;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewsWritten;
}
