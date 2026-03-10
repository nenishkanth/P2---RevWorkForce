package com.revworkforce.revworkforce.admin.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.GoalRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveRepository;
import com.revworkforce.revworkforce.manager.dto.ManagerDashboardStats;
import com.revworkforce.revworkforce.manager.entity.ReviewStatus;
import com.revworkforce.revworkforce.manager.repository.PerformanceReviewRepository;

@Service
public class AnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final GoalRepository goalRepository;
    private final LeaveRepository leaveRepository;

    public AnalyticsService(EmployeeRepository employeeRepository,
                            PerformanceReviewRepository reviewRepository,
                            GoalRepository goalRepository,
                            LeaveRepository leaveRepository) {

        this.employeeRepository = employeeRepository;
        this.reviewRepository = reviewRepository;
        this.goalRepository = goalRepository;
        this.leaveRepository = leaveRepository;
    }

    public ManagerDashboardStats getManagerStats(Long managerId) {

        long teamSize = employeeRepository.countByReportingManagerId(managerId);

        long pendingReviews = reviewRepository
                .countByEmployee_ReportingManager_IdAndStatus(managerId, ReviewStatus.SUBMITTED);

        long completedGoals = goalRepository
                .countByEmployee_ReportingManager_IdAndStatus(managerId, GoalStatus.COMPLETED);

        Double avgRating = reviewRepository.getAverageRatingByManager(managerId);

        double averageRating = avgRating != null ? avgRating : 0.0;

        return new ManagerDashboardStats(
                teamSize,
                pendingReviews,
                completedGoals,
                averageRating
        );
    }

    /**
     * Builds all data needed by the admin analytics page.
     * Returns a map so the controller can put everything into the model in one call.
     */
    public Map<String, Object> getAdminAnalyticsData() {

        Map<String, Object> data = new LinkedHashMap<>();

        // --- Leaves by Status (for doughnut chart) ---
        List<Object[]> statusRows = leaveRepository.countLeavesByStatus();
        Map<String, Long> leavesByStatus = new LinkedHashMap<>();
        for (Object[] row : statusRows) {
            leavesByStatus.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        data.put("leavesByStatus", leavesByStatus);

        // --- Leaves by Type (for bar chart) ---
        List<Object[]> typeRows = leaveRepository.countLeavesByType();
        Map<String, Long> leavesByType = new LinkedHashMap<>();
        for (Object[] row : typeRows) {
            leavesByType.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        data.put("leavesByType", leavesByType);

        // --- Monthly leave trend — current year (for line chart) ---
        // Produce a full 12-month array; months with no data default to 0
        List<Object[]> monthlyRows = leaveRepository.getMonthlyLeaveTrendCurrentYear();
        long[] monthlyTrend = new long[12];
        for (Object[] row : monthlyRows) {
            int month = ((Number) row[0]).intValue(); // 1–12
            long count = ((Number) row[1]).longValue();
            monthlyTrend[month - 1] = count;
        }
        data.put("monthlyTrend", monthlyTrend);

        // --- Department-wise leave distribution (for horizontal bar chart) ---
        List<Object[]> deptRows = leaveRepository.getLeavesGroupedByDepartment();
        Map<String, Long> leavesByDept = new LinkedHashMap<>();
        for (Object[] row : deptRows) {
            leavesByDept.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        data.put("leavesByDept", leavesByDept);

        return data;
    }
}