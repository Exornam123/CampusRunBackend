package com.example.campusrunbackend.service;

import com.example.campusrunbackend.model.CampusService;
import com.example.campusrunbackend.model.ServiceCategory;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.CampusServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CampusServiceService {

    private final CampusServiceRepository serviceRepository;

    @Autowired
    public CampusServiceService(CampusServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public CampusService createService(CampusService service) {
        return serviceRepository.save(service);
    }

    public Optional<CampusService> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public List<CampusService> getAllServices() {
        return serviceRepository.findAll();
    }

    public List<CampusService> getServicesByProvider(User provider) {
        return serviceRepository.findByProvider(provider);
    }

    public List<CampusService> getServicesByCategory(ServiceCategory category) {
        return serviceRepository.findActiveServicesWithFallback(category, category.getName());
    }

    public CampusService updateService(CampusService service) {
        return serviceRepository.save(service);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}
