package com.revworkforce.revworkforce.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.admin.entity.Announcement;
import com.revworkforce.revworkforce.admin.repository.AnnouncementRepository;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repository;

    public AnnouncementService(AnnouncementRepository repository) {
        this.repository = repository;
    }

    // 🔹 Admin creates announcement
    public Announcement createAnnouncement(Announcement announcement, String adminEmail) {

        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setCreatedBy(adminEmail);

        return repository.save(announcement);
    }

    // 🔹 Everyone views announcements
    public List<Announcement> getAllAnnouncements() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    // 🔹 Admin delete
    public void deleteAnnouncement(Long id) {
        repository.deleteById(id);
    }
}