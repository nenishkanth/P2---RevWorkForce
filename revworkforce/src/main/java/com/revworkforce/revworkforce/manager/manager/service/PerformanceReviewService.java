package com.revworkforce.revworkforce.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.employee.entity.Employee;
import com.revworkforce.revworkforce.employee.repository.EmployeeRepository;
import com.revworkforce.revworkforce.manager.entity.PerformanceReview;
import com.revworkforce.revworkforce.manager.entity.ReviewStatus;
import com.revworkforce.revworkforce.manager.repository.PerformanceReviewRepository;
import com.revworkforce.revworkforce.notification.service.NotificationService;

@Service
public class PerformanceReviewService {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public PerformanceReviewService(
            PerformanceReviewRepository reviewRepository,
            EmployeeRepository employeeRepository,
            NotificationService notificationService) {

        this.reviewRepository = reviewRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    // 🔹 Employee submits review
    public PerformanceReview submitReview(Long employeeId,
                                          PerformanceReview review) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        review.setEmployee(employee);
        review.setManager(employee.getReportingManager()); // 🔥 Set manager
        review.setStatus(ReviewStatus.SUBMITTED);
        review.setSubmittedAt(LocalDateTime.now());

        PerformanceReview saved = reviewRepository.save(review);

        // 🔔 Notify Manager
        if (employee.getReportingManager() != null) {
            notificationService.createNotification(
                    employee.getReportingManager(),
                    "New performance review submitted by "
                            + employee.getEmployeeId()
            );
        }
        
        if (review.getSelfRating() == null) {
            throw new RuntimeException("Self rating is required");
        }

        return saved;
    }

    // 🔹 Manager evaluates review
    public PerformanceReview evaluateReview(Long reviewId,
                                            Integer managerRating,
                                            String feedback,
                                            Long managerId) {

        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // 🔒 Security Check
        if (review.getEmployee().getReportingManager() == null ||
            !review.getEmployee()
                   .getReportingManager()
                   .getId()
                   .equals(managerId)) {

            throw new RuntimeException("Unauthorized evaluation");
        }

        review.setManagerRating(managerRating);   // ✅ Correct field
        review.setManagerFeedback(feedback);
        review.setStatus(ReviewStatus.REVIEWED);
        review.setReviewedAt(LocalDateTime.now());

        PerformanceReview saved = reviewRepository.save(review);

        // 🔔 Notify Employee
        notificationService.createNotification(
                review.getEmployee(),
                "Your performance review has been evaluated."
        );

        return saved;
    }

    // 🔹 Employee views own reviews
    public List<PerformanceReview> getEmployeeReviews(Long employeeId) {
        return reviewRepository.findByEmployeeId(employeeId);
    }

    // 🔹 Manager views team reviews
    public List<PerformanceReview> getTeamReviews(Long managerId) {
        return reviewRepository.findByEmployee_ReportingManager_Id(managerId);
    }
}