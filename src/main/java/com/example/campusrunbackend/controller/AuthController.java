package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.AuthRequest;
import com.example.campusrunbackend.dto.AuthResponse;
import com.example.campusrunbackend.dto.RegisterRequest;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.security.JwtUtil;
import com.example.campusrunbackend.service.CustomUserDetailsService;
import com.example.campusrunbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                          CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Validate Index Number based on Qualification
        String qualification = registerRequest.getQualification();
        String index = registerRequest.getIndexNumber() != null ? registerRequest.getIndexNumber().toUpperCase() : null;

        if (index == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Index number is required"));
        }

        if (qualification != null) {
            if (qualification.equalsIgnoreCase("B.Tech") || qualification.equalsIgnoreCase("Diploma")) {
                if (!index.matches("^[A-Z]{2}/[A-Z]{3,4}/\\d{2}/\\d{3,4}$")) {
                    return ResponseEntity.status(400).body(ApiResponse.error("Enter a valid index number (e.g. BC/ITN/23/213)"));
                }
            } else if (qualification.equalsIgnoreCase("HND")) {
                if (!index.matches("^\\d{10}$")) {
                    return ResponseEntity.status(400).body(ApiResponse.error("Enter a valid 10-digit index number"));
                }
            }
        }

        if (userService.getUserByIndexNumber(index).isPresent()) {
            return ResponseEntity.status(400).body(ApiResponse.error("Index number is already registered"));
        }
        if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body(ApiResponse.error("Email is already registered"));
        }
        if (userService.getUserByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body(ApiResponse.error("Username is already taken"));
        }


        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setIndexNumber(index);

        user.setPassword(registerRequest.getPassword());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(registerRequest.getRole());
        user.setQualification(qualification);

        User savedUser = userService.registerUser(user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getIndexNumber());
        final String token = jwtUtil.generateToken(userDetails, savedUser.getRole().name());

        AuthResponse data = new AuthResponse(
                token,
                savedUser.getIndexNumber(),
                savedUser.getRole().name(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getProviderStatus() != null ? savedUser.getProviderStatus().name() : null,
                savedUser.getProfilePicture()
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        // Check if user exists before authenticating to provide specific message
        if (userService.getUserByIndexNumber(authRequest.getIndexNumber()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Account does not exist. Please create a new account."));
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getIndexNumber(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getIndexNumber());

        User user = userService.getUserByIndexNumber(authRequest.getIndexNumber())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        final String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        AuthResponse data = new AuthResponse(
                token,
                user.getIndexNumber(),
                user.getRole().name(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getId(),
                user.getProviderStatus() != null ? user.getProviderStatus().name() : null,
                user.getProfilePicture()
        );

        return ResponseEntity.ok(ApiResponse.success(data, "Login successful"));
    }
}
