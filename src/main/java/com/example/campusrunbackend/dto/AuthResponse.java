package com.example.campusrunbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String indexNumber;
    private String role;
    private String username;
    private String name;
    private String email;
    private Long userId;
    private String providerStatus;
    private String profilePicture;
}
