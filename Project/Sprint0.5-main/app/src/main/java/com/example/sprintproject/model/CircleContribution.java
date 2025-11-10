package com.example.sprintproject.model;

import java.util.Date;

/**
 * Represents a contribution made by a member to a savings circle.
 */
public class CircleContribution {
    private String id;
    private String circleId;
    private String userId;
    private double amount;
    private Date date;
    private String notes;
    private Date createdAt;

    /** Required empty constructor for Firestore. */
    public CircleContribution() {
    }

    /**
     * Constructor for creating a new contribution.
     * @param circleId The ID of the savings circle
     * @param userId The ID of the user making the contribution
     * @param amount The contribution amount
     * @param date The date of the contribution
     * @param notes Additional notes for the contribution
     */
    public CircleContribution(String circleId, String userId, double amount,
                             Date date, String notes) {
        this.circleId = circleId;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CircleContribution{"
                + "id='" + id + '\''
                + ", circleId='" + circleId + '\''
                + ", userId='" + userId + '\''
                + ", amount=" + amount
                + ", date=" + date
                + '}';
    }
}

