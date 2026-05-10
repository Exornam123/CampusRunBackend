package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.CategoryResponseDto;
import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ServiceCategoryService categoryService;

    @Autowired
    public CategoryController(ServiceCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        List<ServiceCategory> categories = categoryService.getAllCategories();
        List<CategoryResponseDto> dtos = categories.stream()
                .map(cat -> new CategoryResponseDto(cat.getId(), cat.getName(), cat.getDescription()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "Categories retrieved successfully"));
    }
}
