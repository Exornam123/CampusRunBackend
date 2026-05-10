package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepository;

    @Autowired
    public ServiceCategoryService(ServiceCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ServiceCategory createCategory(ServiceCategory category) {
        return categoryRepository.save(category);
    }

    public Optional<ServiceCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<ServiceCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public ServiceCategory updateCategory(ServiceCategory category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
