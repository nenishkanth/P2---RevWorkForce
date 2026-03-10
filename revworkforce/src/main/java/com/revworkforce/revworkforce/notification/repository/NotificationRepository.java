package com.revworkforce.revworkforce.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Long countByEmployeeIdAndIsReadFalse(Long employeeId);
    
    List<Notification> findTop5ByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    
    
    
    
}