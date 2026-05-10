package com.example.campusrunbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponseDto {
    private Long id;
    private Long userId;
    private String name;
    private String username;
    private String bio;
    private String location;
    private List<String> categoryNames;
    private String cvUrl;
    private String proofUrl;
    private String providerStatus;
    private boolean online;
}
