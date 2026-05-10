package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.CampusService;
import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusServiceRepository extends JpaRepository<CampusService, Long> {

    @EntityGraph(attributePaths = {"provider", "category"})
    List<CampusService> findByProvider(User provider);

    @EntityGraph(attributePaths = {"provider", "category"})
    List<CampusService> findByCategory(ServiceCategory category);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM CampusService s WHERE (s.category = :category OR s.category.name = :categoryName) AND s.provider.providerStatus = com.example.campusrunbackend.model.ProviderStatus.APPROVED AND s.provider.isOnline = true")
    List<CampusService> findActiveServicesWithFallback(@org.springframework.data.repository.query.Param("category") ServiceCategory category, @org.springframework.data.repository.query.Param("categoryName") String categoryName);


    @Override
    @EntityGraph(attributePaths = {"provider", "category"})
    Optional<CampusService> findById(Long id);
}
