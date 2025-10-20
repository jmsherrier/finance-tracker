package com.example.sprintproject.model;

import java.util.Date;

/**
 * Represents a user budget entry stored in Firestore under users/{uid}/budgets/{id}.
 */
public class Budget {
    private String id;
    private String title;
    private double totalAmount;
    private String category;
    private String frequency; // "weekly" or "monthly"
    private Date startDate;
    private Date createdAt;
    private String userId;
    private double spentAmount;

    // Required empty constructor for Firestore
    public Budget() {}

    // Constructor used when creating a new budget
    public Budget(String title, double totalAmount, String category,
                  String frequency, Date startDate, String userId) {
        this.title = title;
        this.totalAmount = totalAmount;
        this.category = category;
        this.frequency = frequency;
        this.startDate = startDate;
        this.userId = userId;
        this.createdAt = new Date();
        this.spentAmount = 0.0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    public double getRemainingAmount() {
        return totalAmount - spentAmount;
    }

    public double getUtilizationPercentage() {
        if (totalAmount == 0) return 0;
        return (spentAmount / totalAmount) * 100;
    }

    public String getStatusColor() {
        double percentage = getUtilizationPercentage();
        if (percentage >= 90) return "red";
        if (percentage >= 70) return "yellow";
        return "green";
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", totalAmount=" + totalAmount +
                ", category='" + category + '\'' +
                ", frequency='" + frequency + '\'' +
                ", startDate=" + startDate +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", spentAmount=" + spentAmount +
                '}';
    }
}
