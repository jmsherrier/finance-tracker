package com.example.sprintproject.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Represents a user budget entry stored in Firestore under users/{uid}/budgets/{id}.
 */
public class Budget {

    private String id;
    private String title;
    private double amount;
    private String category;    // e.g., Food & Dining, Transportation
    private String frequency;   // "weekly" or "monthly"
    private Date startDate;     // first day of week or month
    private String userId;

    @ServerTimestamp
    private Date createdAt;

    // Required empty constructor for Firestore
    public Budget() {}

    // Constructor used when creating a new budget
    public Budget(String title, double amount, String category,
                  String frequency, Date startDate, String userId) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.frequency = frequency;
        this.startDate = startDate;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Budget{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", frequency='" + frequency + '\'' +
                ", startDate=" + startDate +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
