package com.example.campusrunbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRegistrationDto {
    @NotBlank(message = "Bio is required")
    private String bio;

    @NotBlank(message = "Location is required")
    private String location;

    private java.util.List<Long> categoryIds;
    private java.util.List<ServiceSelectionDto> serviceSelections;
}
