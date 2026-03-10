package com.revworkforce.revworkforce.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByEmployeeId(Long employeeId);

    List<Goal> findByEmployee_ReportingManager_Id(Long managerId);
    
    long countByEmployee_ReportingManager_IdAndStatus(Long managerId, GoalStatus status);
    
    Long countByEmployeeIdAndStatus(Long employeeId, GoalStatus status);
}