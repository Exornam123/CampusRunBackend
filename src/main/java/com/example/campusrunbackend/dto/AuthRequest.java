package com.example.campusrunbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Index number is required")
    private String indexNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
