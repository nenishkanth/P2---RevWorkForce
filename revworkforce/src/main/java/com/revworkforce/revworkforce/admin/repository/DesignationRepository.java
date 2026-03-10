package com.revworkforce.revworkforce.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.admin.entity.Designation;

public interface DesignationRepository extends JpaRepository<Designation, Long> {
}