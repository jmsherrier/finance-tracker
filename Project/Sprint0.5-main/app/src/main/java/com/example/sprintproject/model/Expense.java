package com.example.sprintproject.model;

import java.util.Date;

public class Expense {
    private String id;
    private String name;
    private double amount;
    private String category;
    private Date date;
    private String notes;
    private String userId;
    private Date createdAt;

    // Default constructor for Firestore
    public Expense() { }

    // Constructor for creating new expenses
    public Expense(String name, double amount, String category, Date date,
                   String notes, String userId) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.userId = userId;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
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

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Expense{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", amount=" + amount
                + ", category='" + category + '\''
                + ", date=" + date
                + ", notes='" + notes + '\''
                + ", userId='" + userId + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
