package com.revworkforce.revworkforce.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.revworkforce.revworkforce.manager.entity.PerformanceReview;
import com.revworkforce.revworkforce.manager.entity.ReviewStatus;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

List<PerformanceReview> findByEmployeeId(Long employeeId);

List<PerformanceReview> findByEmployee_ReportingManager_Id(Long managerId);

long countByEmployee_ReportingManager_IdAndStatus(Long managerId, ReviewStatus status);

@Query("SELECT AVG(r.managerRating) FROM PerformanceReview r WHERE r.employee.reportingManager.id = :managerId AND r.status = 'REVIEWED'")
Double getAverageRatingByManager(@Param("managerId") Long managerId);

PerformanceReview findTopByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);


}
