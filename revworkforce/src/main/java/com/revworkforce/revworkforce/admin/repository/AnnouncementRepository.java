package com.revworkforce.revworkforce.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.admin.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();
}