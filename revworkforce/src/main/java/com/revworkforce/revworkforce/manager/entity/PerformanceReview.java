package com.revworkforce.revworkforce.manager.entity;

import java.time.LocalDateTime;

import com.revworkforce.revworkforce.employee.entity.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee who is being reviewed
    @ManyToOne
    private Employee employee;

    // Reporting Manager
    @ManyToOne
    private Employee manager;

    private String reviewYear; // Example: "2026"

    @Column(length = 2000)
    private String deliverables;

    @Column(length = 2000)
    private String accomplishments;

    @Column(length = 2000)
    private String improvementAreas;

    private Integer selfRating;      // Given by employee

    private Integer managerRating;   // Given by manager

    @Column(length = 2000)
    private String managerFeedback;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status; // DRAFT, SUBMITTED, REVIEWED

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    // ===== Getters & Setters =====

    public Long getId() { return id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }

    public String getReviewYear() { return reviewYear; }
    public void setReviewYear(String reviewYear) { this.reviewYear = reviewYear; }

    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }

    public String getAccomplishments() { return accomplishments; }
    public void setAccomplishments(String accomplishments) { this.accomplishments = accomplishments; }

    public String getImprovementAreas() { return improvementAreas; }
    public void setImprovementAreas(String improvementAreas) { this.improvementAreas = improvementAreas; }

    public Integer getSelfRating() { return selfRating; }
    public void setSelfRating(Integer selfRating) { this.selfRating = selfRating; }

    public Integer getManagerRating() { return managerRating; }
    public void setManagerRating(Integer managerRating) { this.managerRating = managerRating; }

    public String getManagerFeedback() { return managerFeedback; }
    public void setManagerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}