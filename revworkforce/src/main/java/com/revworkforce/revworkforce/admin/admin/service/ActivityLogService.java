package com.revworkforce.revworkforce.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.admin.entity.ActivityLog;
import com.revworkforce.revworkforce.admin.repository.ActivityLogRepository;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    public void logActivity(String username, String role, String action, String description) {

        ActivityLog log = new ActivityLog();

        log.setUsername(username);
        log.setRole(role);
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());

        repository.save(log);
    }
    
    public List<ActivityLog> getAllLogs() {
        return repository.findAll();
    }
}