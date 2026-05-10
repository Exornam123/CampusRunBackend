package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.ProviderProfile;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {

    Optional<ProviderProfile> findByUserId(Long userId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.providedServices s " +
            "JOIN u.providerProfile p " +
            "WHERE u.role = com.example.campusrunbackend.model.Role.PROVIDER " +
            "AND u.isOnline = true " +
            "AND u.providerStatus = :status " +
            "AND (s.category.id = :categoryId OR EXISTS (SELECT cat FROM p.categories cat WHERE cat.id = :categoryId))")
    List<User> findOnlineProvidersByCategory(@Param("categoryId") Long categoryId,
                                             @Param("status") com.example.campusrunbackend.model.ProviderStatus status);

    @Query("SELECT p FROM ProviderProfile p JOIN FETCH p.user u WHERE u.providerStatus = :status")
    List<ProviderProfile> findAllByStatusWithUser(@Param("status") com.example.campusrunbackend.model.ProviderStatus status);
}

