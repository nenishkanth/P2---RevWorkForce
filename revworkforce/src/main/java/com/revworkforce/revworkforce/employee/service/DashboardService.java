package com.revworkforce.revworkforce.employee.service;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.employee.dto.EmployeeDashboardResponse;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.repository.GoalRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveBalanceRepository;
import com.revworkforce.revworkforce.employee.repository.LeaveRepository;
import com.revworkforce.revworkforce.manager.dto.ManagerDashboardResponse;
import com.revworkforce.revworkforce.manager.entity.PerformanceReview;
import com.revworkforce.revworkforce.manager.entity.ReviewStatus;
import com.revworkforce.revworkforce.manager.repository.PerformanceReviewRepository;
import com.revworkforce.revworkforce.notification.repository.NotificationRepository;

@Service
public class DashboardService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRepository leaveRepository;
    private final GoalRepository goalRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    

    public DashboardService(
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveRepository leaveRepository,
            GoalRepository goalRepository,
            PerformanceReviewRepository reviewRepository,
            NotificationRepository notificationRepository,
            EmployeeRepository employeeRepository) {

        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRepository = leaveRepository;
        this.goalRepository = goalRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    public EmployeeDashboardResponse getEmployeeDashboard(Employee employee) {

        EmployeeDashboardResponse response = new EmployeeDashboardResponse();

        response.setEmployeeId(employee.getEmployeeId());
        response.setEmail(employee.getEmail());

        // Leave Balance
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeId(employee.getId())
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        response.setCasualLeave(balance.getCasualLeave());
        response.setSickLeave(balance.getSickLeave());
        response.setPaidLeave(balance.getPaidLeave());

        // Pending Leaves Count
        response.setPendingLeaves(
                leaveRepository.countByEmployeeIdAndStatus(
                        employee.getId(),
                        LeaveStatus.PENDING
                )
        );

        // Goals
        response.setActiveGoals(
                goalRepository.countByEmployeeIdAndStatus(
                        employee.getId(),
                        GoalStatus.IN_PROGRESS
                )
        );

        response.setCompletedGoals(
                goalRepository.countByEmployeeIdAndStatus(
                        employee.getId(),
                        GoalStatus.COMPLETED
                )
        );

        // Latest Review
        PerformanceReview latestReview =
                reviewRepository.findTopByEmployeeIdOrderBySubmittedAtDesc(employee.getId());

        if (latestReview != null) {
            response.setLatestReviewStatus(latestReview.getStatus().name());
        }

        // Notifications
        response.setUnreadNotifications(
                notificationRepository.countByEmployeeIdAndIsReadFalse(employee.getId())
        );

        return response;
    }
    
    public ManagerDashboardResponse getManagerDashboard(Employee manager) {

        ManagerDashboardResponse response = new ManagerDashboardResponse();

        Long managerId = manager.getId();

        response.setTotalTeamMembers(
                employeeRepository.countByReportingManagerId(managerId)
        );

        response.setPendingLeaves(
                leaveRepository.countByEmployee_ReportingManager_IdAndStatus(
                        managerId,
                        LeaveStatus.PENDING
                )
        );

        response.setTotalTeamLeaves(
                leaveRepository.countByEmployee_ReportingManager_Id(managerId)
        );

        response.setPendingReviews(
                reviewRepository.countByEmployee_ReportingManager_IdAndStatus(
                        managerId,
                        ReviewStatus.SUBMITTED
                )
        );

        response.setUnreadNotifications(
                notificationRepository.countByEmployeeIdAndIsReadFalse(managerId)
        );

        return response;
    }
}