package com.revworkforce.revworkforce.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.admin.entity.Announcement;
import com.revworkforce.revworkforce.admin.service.AnalyticsService;
import com.revworkforce.revworkforce.admin.service.AnnouncementService;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.GoalStatus;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.service.DashboardService;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.GoalService;
import com.revworkforce.revworkforce.employee.service.LeaveService;
import com.revworkforce.revworkforce.manager.dto.ManagerDashboardResponse;
import com.revworkforce.revworkforce.manager.dto.ManagerDashboardStats;
import com.revworkforce.revworkforce.manager.dto.ReviewEvaluationRequest;
import com.revworkforce.revworkforce.manager.entity.PerformanceReview;
import com.revworkforce.revworkforce.manager.service.PerformanceReviewService;
import com.revworkforce.revworkforce.notification.entity.Notification;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final GoalService goalService;
    private final PerformanceReviewService performanceReviewService;
    private final AnalyticsService analyticsService;
    private final DashboardService dashboardService;
    private final AnnouncementService announcementService;

    public ManagerController(EmployeeService employeeService,
            LeaveService leaveService,
            EmployeeRepository employeeRepository,
            NotificationService notificationService,
            GoalService goalService,
            PerformanceReviewService performanceReviewService,
            AnalyticsService analyticsService,
            DashboardService dashboardService,
            AnnouncementService announcementService) {

    		this.employeeService = employeeService;
    		this.leaveService = leaveService;
    		this.employeeRepository = employeeRepository;
    		this.notificationService = notificationService;
    		this.goalService = goalService;
    		this.performanceReviewService = performanceReviewService;
    		this.analyticsService = analyticsService;
    		this.dashboardService = dashboardService;
    		this.announcementService = announcementService;
    }


    

    // 🔹 View Team Members
    @GetMapping("/{managerId}/team")
    public List<Employee> viewTeam(@PathVariable Long managerId) {
        return employeeService.getTeamMembers(managerId);
    }

    @GetMapping("/leave-requests")
    public List<Leave> viewTeamLeaves() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee manager = employeeService.getEmployeeByEmail(email);

        return leaveService.getTeamLeaves(manager.getId());
    }

    @PutMapping("/leave/{leaveId}/approve")
    public Leave approveLeave(@PathVariable Long leaveId,
                              @RequestParam String comment) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee manager = employeeService.getEmployeeByEmail(email);

        return leaveService.approveLeave(leaveId, comment, manager.getId());
    }

    @PutMapping("/leave/{leaveId}/reject")
    public Leave rejectLeave(@PathVariable Long leaveId,
                             @RequestParam String comment) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee manager = employeeService.getEmployeeByEmail(email);

        return leaveService.rejectLeave(leaveId, comment, manager.getId());
    }
    @GetMapping("/team-leaves")
    public List<Leave> viewTeamLeaves(@RequestParam(required = false) LeaveStatus status,
                                      Authentication authentication) {

        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (status != null) {
            return leaveService.getTeamLeavesByStatus(manager.getId(), status);
        }

        return leaveService.getTeamLeaves(manager.getId());
    }
    
    @GetMapping("/team-leaves/stats")
    public Map<String, Long> getTeamLeaveStats(Authentication authentication) {

        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return leaveService.getTeamLeaveStats(manager.getId());
    }
    
    @GetMapping("/notifications")
    public List<Notification> getNotifications(Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return notificationService.getNotifications(employee.getId());
    }
    
 // 🔹 View Team Goals
    @GetMapping("/goals")
    public List<Goal> viewTeamGoals(Authentication authentication) {

        Employee manager = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return goalService.getTeamGoals(manager.getId());
    }

    // 🔹 Update Goal Status
    @PutMapping("/goal/{goalId}/status")
    public Goal updateGoalStatus(@PathVariable Long goalId,
                                 @RequestParam GoalStatus status,
                                 Authentication authentication) {

        Employee manager = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return goalService.updateGoalStatus(goalId, status, manager.getId());
    }
    
    @GetMapping("/reviews")
    public List<PerformanceReview> getTeamReviews(Authentication authentication) {

        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return performanceReviewService.getTeamReviews(manager.getId());
    }
    
    @PutMapping("/reviews/{reviewId}/evaluate")
    public PerformanceReview evaluateReview(@PathVariable Long reviewId,
                                            @RequestBody ReviewEvaluationRequest request,
                                            Authentication authentication) {

        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return performanceReviewService.evaluateReview(
                reviewId,
                request.getManagerRating(),
                request.getManagerFeedback(),
                manager.getId()
        );
    }
    
    @GetMapping("/dashboard/stats")
    public ManagerDashboardStats getDashboardStats(Authentication authentication) {

        Employee manager = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return analyticsService.getManagerStats(manager.getId());
    }
    
    @GetMapping("/dashboard")
    public ManagerDashboardResponse getDashboard(Authentication authentication) {

        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        return dashboardService.getManagerDashboard(manager);
    }
    
    @GetMapping("/announcements")
    public List<Announcement> getAnnouncements() {
        return announcementService.getAllAnnouncements();
    }
}