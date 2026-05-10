package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String indexNumber) throws UsernameNotFoundException {
        User user = userRepository.findByIndexNumber(indexNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with index number: " + indexNumber));

        return new org.springframework.security.core.userdetails.User(
                user.getIndexNumber(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
