package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.ProviderPromotion;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderPromotionRepository extends JpaRepository<ProviderPromotion, Long> {
    List<ProviderPromotion> findAllByProviderAndActive(User provider, boolean active);
    List<ProviderPromotion> findAllByProviderInAndActive(List<User> providers, boolean active);
}
