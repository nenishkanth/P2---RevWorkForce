package com.revworkforce.revworkforce.employee.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.admin.entity.Announcement;
import com.revworkforce.revworkforce.admin.service.AnnouncementService;
import com.revworkforce.revworkforce.employee.dto.EmployeeDashboardResponse;
import com.revworkforce.revworkforce.employee.dto.EmployeeProfileUpdateRequest;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.Goal;
import com.revworkforce.revworkforce.employee.entity.Leave;
import com.revworkforce.revworkforce.employee.entity.LeaveBalance;
import com.revworkforce.revworkforce.employee.entity.LeaveStatus;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.employee.service.DashboardService;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.GoalService;
import com.revworkforce.revworkforce.employee.service.LeaveService;
import com.revworkforce.revworkforce.manager.entity.PerformanceReview;
import com.revworkforce.revworkforce.manager.service.PerformanceReviewService;
import com.revworkforce.revworkforce.notification.entity.Notification;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    
    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final GoalService goalService;
    private final PerformanceReviewService performanceReviewService;
    private final DashboardService dashboardService;
    private final AnnouncementService announcementService;
    private final EmployeeService employeeService;
    
  
    

    public EmployeeController(LeaveService leaveService,
                              EmployeeRepository employeeRepository,
                              NotificationService notificationService,
                              GoalService goalService,
                              PerformanceReviewService performanceReviewService,
                              DashboardService dashboardService,
                              AnnouncementService announcementService,
                              EmployeeService employeeService) {
    	
        this.leaveService = leaveService;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
        this.goalService = goalService;
        this.performanceReviewService = performanceReviewService;
        this.dashboardService = dashboardService;
        this.announcementService = announcementService;
        this.employeeService = employeeService;
    }

    @PostMapping("/apply-leave")
    public Leave applyLeave(@RequestBody Leave leave) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveService.applyLeave(employee.getId(), leave);
    }

    @GetMapping("/{employeeId}/leaves")
    public List<Leave> viewLeaves(@PathVariable Long employeeId) {
        return leaveService.getEmployeeLeaves(employeeId);
    }
    
    @PutMapping("/leave/{leaveId}/cancel")
    public Leave cancelLeave(@PathVariable Long leaveId) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveService.cancelLeave(leaveId, employee.getId());
    }
    
    @GetMapping("/leave-balance")
    public LeaveBalance viewLeaveBalance() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveService.getLeaveBalance(employee.getId());
    }
    
    @GetMapping("/leaves")
    public List<Leave> getMyLeaves(@RequestParam(required = false) LeaveStatus status) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (status != null) {
            return leaveService.getEmployeeLeavesByStatus(employee.getId(), status);
        }

        return leaveService.getEmployeeLeaves(employee.getId());
    }
    @GetMapping("/notifications")
    public List<Notification> getNotifications(Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return notificationService.getNotifications(employee.getId());
    }
 // 🔹 Add Goal
    @PostMapping("/goals")
    public Goal addGoal(@RequestBody Goal goal,
                        Authentication authentication) {

        Employee employee = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return goalService.addGoal(employee.getId(), goal);
    }

    // 🔹 View My Goals
    @GetMapping("/goals")
    public List<Goal> viewMyGoals(Authentication authentication) {

        Employee employee = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return goalService.getEmployeeGoals(employee.getId());
    }
    
    @GetMapping("/announcements")
    public List<Announcement> getAnnouncements() {
        return announcementService.getAllAnnouncements();
    }
    
    @PostMapping("/reviews")
    public PerformanceReview submitReview(@RequestBody PerformanceReview review,
                                          Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return performanceReviewService.submitReview(employee.getId(), review);
    }
    
    @GetMapping("/reviews")
    public List<PerformanceReview> getMyReviews(Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return performanceReviewService.getEmployeeReviews(employee.getId());
    }
    
    @GetMapping("/dashboard")
    public EmployeeDashboardResponse getDashboard(Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return dashboardService.getEmployeeDashboard(employee);
    }
    
    @GetMapping("/profile")
    public Employee viewProfile(Authentication authentication) {

        String email = authentication.getName();

        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
    
    @PutMapping("/profile")
    public Employee updateProfile(@RequestBody EmployeeProfileUpdateRequest request,
                                  Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return employeeService.updateProfile(employee.getId(), request);
    }
    
    @PutMapping("/notifications/{id}/read")
    public String markNotificationAsRead(@PathVariable Long id,
                                         Authentication authentication) {

        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        notificationService.markAsRead(id, employee.getId());

        return "Notification marked as read";
    }
    
    
}