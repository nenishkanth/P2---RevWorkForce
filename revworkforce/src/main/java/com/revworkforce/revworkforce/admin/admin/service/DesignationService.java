package com.revworkforce.revworkforce.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.admin.entity.Designation;
import com.revworkforce.revworkforce.admin.repository.DesignationRepository;

@Service
public class DesignationService {

    private final DesignationRepository repository;

    public DesignationService(DesignationRepository repository) {
        this.repository = repository;
    }

    public List<Designation> getAllDesignations() {
        return repository.findAll();
    }

    public Designation saveDesignation(Designation designation) {
        return repository.save(designation);
    }
    
    public Designation getDesignation(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found"));
    }

    public void deleteDesignation(Long id) {
        repository.deleteById(id);
    }
}