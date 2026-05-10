package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIndexNumber(String indexNumber);
    Optional<User> findByUsername(String username);
    java.util.List<User> findByRole(com.example.campusrunbackend.model.Role role);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.providerProfile WHERE u.role = :role AND u.providerStatus = :status")
    java.util.List<User> findAllByRoleAndProviderStatus(@org.springframework.data.repository.query.Param("role") com.example.campusrunbackend.model.Role role, @org.springframework.data.repository.query.Param("status") com.example.campusrunbackend.model.ProviderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.providerProfile WHERE u.id = :id")
    java.util.Optional<User> findByIdWithProfile(@org.springframework.data.repository.query.Param("id") Long id);
}



