package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.ProviderSubscription;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderSubscriptionRepository extends JpaRepository<ProviderSubscription, Long> {
    Optional<ProviderSubscription> findByProviderAndActive(User provider, boolean active);
    List<ProviderSubscription> findAllByProviderInAndActive(List<User> providers, boolean active);
}
