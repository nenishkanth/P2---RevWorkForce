package com.revworkforce.revworkforce.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.admin.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByTimestampDesc();

}