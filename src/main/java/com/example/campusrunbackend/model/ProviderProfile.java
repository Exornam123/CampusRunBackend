package com.example.campusrunbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "provider_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    @Column(nullable = false)
    private String location;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "provider_cvs", joinColumns = @JoinColumn(name = "provider_profile_id"))
    private java.util.Set<Attachment> cvFiles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "provider_proofs", joinColumns = @JoinColumn(name = "provider_profile_id"))
    private java.util.Set<Attachment> proofFiles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'FRESH_HUSTLER'")
    private ProviderRank rank = ProviderRank.FRESH_HUSTLER;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int rankScore = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "provider_services",
            joinColumns = @JoinColumn(name = "provider_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "service_category_id")
    )
    private Set<ServiceCategory> categories;
}
