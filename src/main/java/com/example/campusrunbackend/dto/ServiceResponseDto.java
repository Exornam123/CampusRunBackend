package com.example.campusrunbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDto {
    private Long id;
    private String title;
    private String description;
    private java.math.BigDecimal price;
    private Long categoryId;

    private String categoryName;
    private String providerName;
    private Long providerId;
    private String providerPhone;
}
