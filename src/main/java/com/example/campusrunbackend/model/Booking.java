package com.example.campusrunbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_client", columnList = "client_id"),
        @Index(name = "idx_booking_service", columnList = "service_id"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_client_done", columnList = "client_confirmed_done"),
        @Index(name = "idx_booking_provider_done", columnList = "provider_confirmed_done")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private CampusService service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "client_confirmed_done", nullable = false, columnDefinition = "boolean default false")
    private boolean clientConfirmedDone = false;

    @Column(name = "provider_confirmed_done", nullable = false, columnDefinition = "boolean default false")
    private boolean providerConfirmedDone = false;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new java.util.ArrayList<>();
}
