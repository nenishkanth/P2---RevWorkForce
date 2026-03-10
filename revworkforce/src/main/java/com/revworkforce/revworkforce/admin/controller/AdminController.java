package com.revworkforce.revworkforce.admin.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.admin.entity.Announcement;
import com.revworkforce.revworkforce.admin.entity.Role;
import com.revworkforce.revworkforce.admin.service.AnnouncementService;
import com.revworkforce.revworkforce.calendar.entity.Holiday;
import com.revworkforce.revworkforce.calendar.service.HolidayService;
import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.entity.LeavePolicy;
import com.revworkforce.revworkforce.employee.service.EmployeeService;
import com.revworkforce.revworkforce.employee.service.LeavePolicyService;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "Admin Dashboard - Access Granted";
    }

    @GetMapping("/settings")
    public String adminSettings() {
        return "Admin Settings - Access Granted";
    }
    
    
    private final EmployeeService service;
    private final LeavePolicyService leavePolicyService;
    private final NotificationService notificationService;
    private final HolidayService holidayService;
    private final AnnouncementService announcementService;
    
    public AdminController(EmployeeService service,
            LeavePolicyService leavePolicyService,
            NotificationService notificationService,
            HolidayService holidayService,
            AnnouncementService announcementService) {
    		this.service = service;
    		this.leavePolicyService = leavePolicyService;
    		this.notificationService = notificationService;
    		this.holidayService = holidayService;
    		this.announcementService = announcementService;
    }
    
    @PutMapping("/{employeeId}/assign-manager/{managerId}")
    public Employee assignManager(@PathVariable Long employeeId,
                                  @PathVariable Long managerId) {
        return service.assignManager(employeeId, managerId);
    }
    
    @PutMapping("/leave-policy/{role}")
    public LeavePolicy setLeavePolicy(@PathVariable Role role,
                                      @RequestBody LeavePolicy policy) {

        return leavePolicyService.setPolicy(role, policy);
    }
    @GetMapping("/leave-policy")
    public List<LeavePolicy> getAllPolicies() {
        return leavePolicyService.getAllPolicies();
    }
    
    @GetMapping("/leave-policy/{role}")
    public LeavePolicy getPolicyByRole(@PathVariable Role role) {
        return leavePolicyService.getPolicyByRole(role);
    }
    
    @PostMapping("/broadcast")
    public String broadcast(@RequestParam String message) {
        notificationService.broadcastNotification(message);
        return "Broadcast sent successfully";
    }
    
    @PostMapping("/holidays")
    public Holiday addHoliday(@RequestBody Holiday holiday) {
        return holidayService.addHoliday(holiday);
    }

    @PutMapping("/holidays/{id}")
    public Holiday updateHoliday(@PathVariable Long id,
                                 @RequestBody Holiday holiday) {
        return holidayService.updateHoliday(id, holiday);
    }

    @DeleteMapping("/holidays/{id}")
    public String deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return "Holiday deleted successfully";
    }
    
    @PostMapping("/announcements")
    public Announcement createAnnouncement(@RequestBody Announcement announcement,
                                           Authentication authentication) {

        String adminEmail = authentication.getName();
        return announcementService.createAnnouncement(announcement, adminEmail);
    }
    
    @DeleteMapping("/announcements/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return "Announcement deleted";
    }
}