package com.revworkforce.revworkforce.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.notification.entity.Notification;
import com.revworkforce.revworkforce.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    // 🔔 Create Notification
    public void createNotification(Employee employee, String message) {
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // 📥 Get All Notifications
    public List<Notification> getNotifications(Long employeeId) {
        return notificationRepository
                .findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    // 🔢 Unread Count
    public long getUnreadCount(Long employeeId) {
        return notificationRepository
                .countByEmployeeIdAndIsReadFalse(employeeId);
    }

    // ✅ Mark As Read
    public Notification markAsRead(Long notificationId, Long employeeId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Unauthorized access");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    // ❌ Delete Notification
    public void deleteNotification(Long notificationId, Long employeeId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Unauthorized access");
        }

        notificationRepository.delete(notification);
    }

    // 📢 Admin Broadcast
    public void broadcastNotification(String message) {

        List<Employee> employees = employeeRepository.findAll();

        for (Employee employee : employees) {
            createNotification(employee, message);
        }
    }
    
    public List<Notification> getEmployeeNotifications(Long employeeId) {
        return notificationRepository
                .findTop5ByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }
    
    
}