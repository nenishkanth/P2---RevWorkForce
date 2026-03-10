package com.revworkforce.revworkforce.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.admin.entity.Department;
import com.revworkforce.revworkforce.admin.repository.DepartmentRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<Department> getAllDepartments() {
        return repository.findAll();
    }

    public Department saveDepartment(Department department) {
        return repository.save(department);
    }
    
    public Department getDepartmentById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

    }

    public void deleteDepartment(Long id) {

        repository.deleteById(id);

    }
}