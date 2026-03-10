package com.revworkforce.revworkforce.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {

    Optional<LeavePolicy> findByRole(Role role);
}