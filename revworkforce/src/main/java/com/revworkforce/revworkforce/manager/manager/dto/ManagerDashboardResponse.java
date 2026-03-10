package com.revworkforce.revworkforce.manager.dto;

public class ManagerDashboardResponse {

    private Long totalTeamMembers;
    private Long pendingLeaves;
    private Long totalTeamLeaves;
    private Long pendingReviews;
    private Long unreadNotifications;

    public Long getTotalTeamMembers() { return totalTeamMembers; }
    public void setTotalTeamMembers(Long totalTeamMembers) { this.totalTeamMembers = totalTeamMembers; }

    public Long getPendingLeaves() { return pendingLeaves; }
    public void setPendingLeaves(Long pendingLeaves) { this.pendingLeaves = pendingLeaves; }

    public Long getTotalTeamLeaves() { return totalTeamLeaves; }
    public void setTotalTeamLeaves(Long totalTeamLeaves) { this.totalTeamLeaves = totalTeamLeaves; }

    public Long getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(Long pendingReviews) { this.pendingReviews = pendingReviews; }

    public Long getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(Long unreadNotifications) { this.unreadNotifications = unreadNotifications; }
}