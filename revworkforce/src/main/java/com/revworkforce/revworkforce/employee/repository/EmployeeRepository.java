package com.revworkforce.revworkforce.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeId(String employeeId);

    long countByRole(Role role);   // 🔥 ADD THIS LINE
    
    List<Employee> findByReportingManagerId(Long managerId);
    
    long countByReportingManagerId(Long managerId);
    
    List<Employee> findByRole(Role role);
    
    boolean existsByEmail(String email);
    
    Employee findTopByRoleOrderByEmployeeIdDesc(Role role);
    
    @Query("""
    		SELECT e FROM Employee e
    		LEFT JOIN e.department d
    		LEFT JOIN e.designation g
    		WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		OR LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		OR LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		""")
    		List<Employee> searchEmployees(@Param("keyword") String keyword);
}