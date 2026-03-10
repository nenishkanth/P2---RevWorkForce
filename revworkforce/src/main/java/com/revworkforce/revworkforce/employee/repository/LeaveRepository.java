package com.revworkforce.revworkforce.employee.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveType;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    List<Leave> findByEmployeeId(Long employeeId);

    List<Leave> findByStatus(LeaveStatus status);

    List<Leave> findByEmployee_ReportingManager_Id(Long managerId);

    List<Leave> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    List<Leave> findByEmployeeReportingManagerId(Long managerId);

    List<Leave> findByEmployeeReportingManagerIdAndStatus(Long managerId, LeaveStatus status);
    
    Long countByEmployee_ReportingManager_IdAndLeaveType(Long managerId, LeaveType type);

    @Query("SELECT l.status, COUNT(l) FROM Leave l WHERE l.employee.reportingManager.id = :managerId GROUP BY l.status")
    List<Object[]> countTeamLeavesByStatus(@Param("managerId") Long managerId);

    Long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    Long countByEmployee_ReportingManager_IdAndStatus(Long managerId, LeaveStatus status);

    Long countByEmployee_ReportingManager_Id(Long managerId);

    List<Leave> findAllByOrderByStartDateDesc();

    @Query("""
        SELECT l FROM Leave l
        WHERE (:status IS NULL OR l.status = :status)
        AND (:type IS NULL OR l.leaveType = :type)
        AND (:employeeId IS NULL OR l.employee.id = :employeeId)
        AND (:departmentId IS NULL OR l.employee.department.id = :departmentId)
        AND (:startDate IS NULL OR l.startDate >= :startDate)
        AND (:endDate IS NULL OR l.endDate <= :endDate)
    """)
    List<Leave> filterLeaves(
            @Param("status") LeaveStatus status,
            @Param("type") LeaveType type,
            @Param("departmentId") Long departmentId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT MONTH(l.startDate), COUNT(l)
        FROM Leave l
        GROUP BY MONTH(l.startDate)
        ORDER BY MONTH(l.startDate)
    """)
    List<Object[]> getMonthlyLeaveReport();

    @Query("""
        SELECT l.employee.employeeId,
               l.employee.firstName,
               l.employee.lastName,
               COUNT(l)
        FROM Leave l
        GROUP BY l.employee.employeeId, l.employee.firstName, l.employee.lastName
    """)
    List<Object[]> getEmployeeLeaveReport();

    @Query("""
        SELECT e.department.name, COUNT(l)
        FROM Leave l
        JOIN l.employee e
        GROUP BY e.department.name
    """)
    List<Object[]> getDepartmentLeaveReport();

    @Query("""
        SELECT l
        FROM Leave l
        JOIN l.employee e
        WHERE e.id = :employeeId
        ORDER BY l.startDate DESC
    """)
    List<Leave> findLeavesByEmployeeId(@Param("employeeId") Long employeeId);
    
    
    @Query("""
    		SELECT l FROM Leave l
    		WHERE l.employee.reportingManager.id = :managerId
    		AND l.startDate >= CURRENT_DATE
    		ORDER BY l.startDate
    		""")
    		List<Leave> findUpcomingLeaves(Long managerId);

    // Analytics: count of leaves grouped by status (for admin analytics page)
    @Query("SELECT l.status, COUNT(l) FROM Leave l GROUP BY l.status")
    List<Object[]> countLeavesByStatus();

    // Analytics: count of leaves grouped by leave type (for admin analytics page)
    @Query("SELECT l.leaveType, COUNT(l) FROM Leave l GROUP BY l.leaveType")
    List<Object[]> countLeavesByType();

    // Analytics: monthly leave trend for current year (for admin analytics page)
    @Query("""
            SELECT MONTH(l.startDate), COUNT(l)
            FROM Leave l
            WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)
            GROUP BY MONTH(l.startDate)
            ORDER BY MONTH(l.startDate)
            """)
    List<Object[]> getMonthlyLeaveTrendCurrentYear();

    // Analytics: department-wise leave count (for admin analytics page)
    @Query("""
            SELECT e.department.name, COUNT(l)
            FROM Leave l
            JOIN l.employee e
            WHERE e.department IS NOT NULL
            GROUP BY e.department.name
            ORDER BY COUNT(l) DESC
            """)
    List<Object[]> getLeavesGroupedByDepartment();
}