package com.revworkforce.revworkforce.notification.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.notification.entity.Notification;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;
    private final EmployeeRepository employeeRepository;

    public NotificationController(NotificationService service,
                                  EmployeeRepository employeeRepository) {
        this.service = service;
        this.employeeRepository = employeeRepository;
    }

    // 🔔 Get All Notifications
    @GetMapping
    public List<Notification> getNotifications(Authentication authentication) {

        Employee employee = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return service.getNotifications(employee.getId());
    }

    // 🔢 Unread Count
    @GetMapping("/unread-count")
    public long getUnreadCount(Authentication authentication) {

        Employee employee = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return service.getUnreadCount(employee.getId());
    }

    // ✅ Mark As Read
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id,
                                   Authentication authentication) {

        Employee employee = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return service.markAsRead(id, employee.getId());
    }

    // ❌ Delete
    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable Long id,
                                     Authentication authentication) {

        Employee employee = employeeRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        service.deleteNotification(id, employee.getId());
        return "Notification deleted";
    }
}