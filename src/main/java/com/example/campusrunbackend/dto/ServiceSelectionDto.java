package com.example.campusrunbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSelectionDto {
    private Long categoryId;
    private String serviceTitle;
}
