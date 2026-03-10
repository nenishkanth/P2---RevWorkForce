package com.revworkforce.revworkforce.employee.dto;

public class EmployeeDashboardResponse {

    private String employeeId;
    private String email;

    private Integer casualLeave;
    private Integer sickLeave;
    private Integer paidLeave;

    private Long pendingLeaves;

    private Long activeGoals;
    private Long completedGoals;

    private String latestReviewStatus;

    private Long unreadNotifications;

    // Getters & Setters

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getCasualLeave() { return casualLeave; }
    public void setCasualLeave(Integer casualLeave) { this.casualLeave = casualLeave; }

    public Integer getSickLeave() { return sickLeave; }
    public void setSickLeave(Integer sickLeave) { this.sickLeave = sickLeave; }

    public Integer getPaidLeave() { return paidLeave; }
    public void setPaidLeave(Integer paidLeave) { this.paidLeave = paidLeave; }

    public Long getPendingLeaves() { return pendingLeaves; }
    public void setPendingLeaves(Long pendingLeaves) { this.pendingLeaves = pendingLeaves; }

    public Long getActiveGoals() { return activeGoals; }
    public void setActiveGoals(Long activeGoals) { this.activeGoals = activeGoals; }

    public Long getCompletedGoals() { return completedGoals; }
    public void setCompletedGoals(Long completedGoals) { this.completedGoals = completedGoals; }

    public String getLatestReviewStatus() { return latestReviewStatus; }
    public void setLatestReviewStatus(String latestReviewStatus) { this.latestReviewStatus = latestReviewStatus; }

    public Long getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(Long unreadNotifications) { this.unreadNotifications = unreadNotifications; }
}