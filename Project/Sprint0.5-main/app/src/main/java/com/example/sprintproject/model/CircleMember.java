package com.example.sprintproject.model;

import java.util.Date;

/**
 * Represents a member of a savings circle.
 */
public class CircleMember {
    private String userId;
    private String email;
    private String circleId;
    private String role; // "leader" or "member"
    private Date joinedAt;
    private Date startDate; // Member's challenge period start
    private Date endDate; // Member's challenge period end
    private double totalContribution;

    /** Required empty constructor for Firestore. */
    public CircleMember() {
    }

    /**
     * Constructor for creating a new circle member.
     * @param userId The user ID
     * @param email The user's email
     * @param circleId The circle ID
     * @param role The member's role
     * @param startDate The start date
     * @param endDate The end date
     */
    public CircleMember(String userId, String email, String circleId,
                       String role, Date startDate, Date endDate) {
        this.userId = userId;
        this.email = email;
        this.circleId = circleId;
        this.role = role;
        this.joinedAt = new Date();
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalContribution = 0.0;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getTotalContribution() {
        return totalContribution;
    }

    public void setTotalContribution(double totalContribution) {
        this.totalContribution = totalContribution;
    }

    /**
     * Calculate contribution percentage relative to goal.
     * @param goalAmount The goal amount
     * @return The contribution percentage (0-100)
     */
    public double getContributionPercentage(double goalAmount) {
        if (goalAmount == 0) {
            return 0;
        }
        return (totalContribution / goalAmount) * 100;
    }

    /**
     * Check if member is currently active in their challenge period.
     * @return true if the member is active, false otherwise
     */
    public boolean isActive() {
        if (startDate == null || endDate == null) {
            return false;
        }
        Date now = new Date();
        return !now.before(startDate) && !now.after(endDate);
    }

    /**
     * Check if member is the leader.
     * @return true if the member is a leader, false otherwise
     */
    public boolean isLeader() {
        return "leader".equals(role);
    }

    @Override
    public String toString() {
        return "CircleMember{"
                + "userId='" + userId + '\''
                + ", email='" + email + '\''
                + ", role='" + role + '\''
                + ", totalContribution=" + totalContribution
                + '}';
    }
}

