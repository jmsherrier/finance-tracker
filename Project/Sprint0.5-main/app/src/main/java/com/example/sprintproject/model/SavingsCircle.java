package com.example.sprintproject.model;

import java.util.Date;

/**
 * Represents a savings circle (group savings challenge) stored in Firestore.
 */
public class SavingsCircle {
    private String id;
    private String groupName;
    private String creatorEmail;
    private String creatorId;
    private String challengeTitle;
    private double goalAmount;
    private String frequency; // "weekly" or "monthly"
    private Date startDate;
    private Date endDate;
    private String notes;
    private String status; // "active", "completed", "cancelled"
    private Date createdAt;

    /** Required empty constructor for Firestore. */
    public SavingsCircle() {
    }

    /**
     * Constructor for creating a new savings circle.
     */
    public SavingsCircle(String groupName, String creatorEmail, String creatorId,
                        String challengeTitle, double goalAmount, String frequency,
                        Date startDate, String notes) {
        this.groupName = groupName;
        this.creatorEmail = creatorEmail;
        this.creatorId = creatorId;
        this.challengeTitle = challengeTitle;
        this.goalAmount = goalAmount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.notes = notes;
        this.status = "active";
        this.createdAt = new Date();
        this.endDate = calculateEndDate(startDate, frequency);
    }

    /**
     * Calculate end date based on start date and frequency.
     */
    private Date calculateEndDate(Date startDate, String frequency) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(startDate);
        
        if ("weekly".equalsIgnoreCase(frequency)) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, 7);
        } else if ("monthly".equalsIgnoreCase(frequency)) {
            cal.add(java.util.Calendar.MONTH, 1);
        }
        
        return cal.getTime();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getChallengeTitle() {
        return challengeTitle;
    }

    public void setChallengeTitle(String challengeTitle) {
        this.challengeTitle = challengeTitle;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
        if (startDate != null) {
            this.endDate = calculateEndDate(startDate, frequency);
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        if (frequency != null) {
            this.endDate = calculateEndDate(startDate, frequency);
        }
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Calculate progress percentage based on current progress.
     */
    public double calculateProgressPercentage(double currentProgress) {
        if (goalAmount == 0) {
            return 0;
        }
        return (currentProgress / goalAmount) * 100;
    }

    /**
     * Get days remaining until end date.
     */
    public long getDaysRemaining() {
        if (endDate == null) {
            return 0;
        }
        Date now = new Date();
        long diff = endDate.getTime() - now.getTime();
        return Math.max(0, diff / (1000 * 60 * 60 * 24));
    }

    /**
     * Check if circle is currently active.
     */
    public boolean isActive() {
        Date now = new Date();
        return "active".equals(status) 
            && startDate != null 
            && endDate != null
            && !now.before(startDate) 
            && !now.after(endDate);
    }

    /**
     * Check if circle goal is complete.
     */
    public boolean isComplete(double currentProgress) {
        return currentProgress >= goalAmount || "completed".equals(status);
    }

    @Override
    public String toString() {
        return "SavingsCircle{"
                + "id='" + id + '\''
                + ", groupName='" + groupName + '\''
                + ", creatorEmail='" + creatorEmail + '\''
                + ", challengeTitle='" + challengeTitle + '\''
                + ", goalAmount=" + goalAmount
                + ", frequency='" + frequency + '\''
                + ", status='" + status + '\''
                + '}';
    }
}

