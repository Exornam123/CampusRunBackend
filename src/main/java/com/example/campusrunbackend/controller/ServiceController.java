package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.dto.ApiResponse;
import com.example.campusrunbackend.dto.ServiceCreateDto;
import com.example.campusrunbackend.dto.ServiceResponseDto;
import com.example.campusrunbackend.model.CampusService;
import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.service.CampusServiceService;
import com.example.campusrunbackend.service.ServiceCategoryService;
import com.example.campusrunbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final CampusServiceService serviceService;
    private final ServiceCategoryService categoryService;
    private final UserService userService;

    @Autowired
    public ServiceController(CampusServiceService serviceService,
                             ServiceCategoryService categoryService,
                             UserService userService) {
        this.serviceService = serviceService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> createService(
            @Valid @RequestBody ServiceCreateDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {

        User provider = userService.getUserByIndexNumber(currentUser.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        ServiceCategory category = categoryService.getCategoryById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID"));

        CampusService service = new CampusService();
        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setCategory(category);
        service.setProvider(provider);
        service.setPrice(dto.getPrice());


        CampusService savedService = serviceService.createService(service);
        ServiceResponseDto data = mapToResponseDto(savedService);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Service created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceCreateDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {

        CampusService existingService = serviceService.getServiceById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        if (!existingService.getProvider().getIndexNumber().equals(currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own services");
        }

        if (dto.getCategoryId() != null) {
            ServiceCategory category = categoryService.getCategoryById(dto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID"));
            existingService.setCategory(category);
        }

        existingService.setTitle(dto.getTitle());
        existingService.setDescription(dto.getDescription());
        existingService.setPrice(dto.getPrice());


        CampusService updatedService = serviceService.updateService(existingService);
        ServiceResponseDto data = mapToResponseDto(updatedService);

        return ResponseEntity.ok(ApiResponse.success(data, "Service updated successfully"));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ServiceResponseDto>>> getServicesByCategory(@PathVariable Long categoryId) {
        ServiceCategory category = categoryService.getCategoryById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        List<CampusService> services = serviceService.getServicesByCategory(category);
        List<ServiceResponseDto> dtos = services.stream()
                .map(service -> mapToResponseDto(service))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "Services retrieved successfully"));
    }

    private ServiceResponseDto mapToResponseDto(CampusService service) {
        return new ServiceResponseDto(
                service.getId(),
                service.getTitle(),
                service.getDescription(),
                service.getPrice(),
                service.getCategory().getId(),
                service.getCategory().getName(),
                service.getProvider().getName(),
                service.getProvider().getId(),
                service.getProvider().getPhoneNumber()

        );
    }
}
