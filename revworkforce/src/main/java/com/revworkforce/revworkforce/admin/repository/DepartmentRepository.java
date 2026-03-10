package com.revworkforce.revworkforce.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.admin.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}