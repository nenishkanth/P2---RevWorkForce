package com.revworkforce.revworkforce.manager.dto;

public class ManagerDashboardStats {

    private long teamSize;
    private long pendingReviews;
    private long completedGoals;
    private double averageTeamRating;

    public ManagerDashboardStats(long teamSize,
                                 long pendingReviews,
                                 long completedGoals,
                                 double averageTeamRating) {
        this.teamSize = teamSize;
        this.pendingReviews = pendingReviews;
        this.completedGoals = completedGoals;
        this.averageTeamRating = averageTeamRating;
    }

    public long getTeamSize() {
        return teamSize;
    }

    public long getPendingReviews() {
        return pendingReviews;
    }

    public long getCompletedGoals() {
        return completedGoals;
    }

    public double getAverageTeamRating() {
        return averageTeamRating;
    }
}