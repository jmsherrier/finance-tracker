package com.example.sprintproject.model;

import java.io.Serializable;

/**
 * Represents a budget warning notification.
 * Contains information about a budget that has reached a threshold percentage.
 */
public class BudgetWarning implements Serializable {
    private static final long serialVersionUID = 1L;
    private String budgetId;
    private String category;
    private double spentAmount;
    private double totalAmount;
    private double percentage;
    private long timestamp;

    /**
     * Default constructor for BudgetWarning.
     */
    public BudgetWarning() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor for creating a new budget warning.
     *
     * @param budgetId the ID of the budget
     * @param category the category name
     * @param spentAmount the amount spent
     * @param totalAmount the total budget amount
     * @param percentage the percentage used
     */
    public BudgetWarning(String budgetId, String category, double spentAmount,
                         double totalAmount, double percentage) {
        this.budgetId = budgetId;
        this.category = category;
        this.spentAmount = spentAmount;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the budget ID.
     *
     * @return the budget ID
     */
    public String getBudgetId() {
        return budgetId;
    }

    /**
     * Sets the budget ID.
     *
     * @param budgetId the budget ID to set
     */
    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    /**
     * Gets the category name.
     *
     * @return the category name
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category name.
     *
     * @param category the category name to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the spent amount.
     *
     * @return the spent amount
     */
    public double getSpentAmount() {
        return spentAmount;
    }

    /**
     * Sets the spent amount.
     *
     * @param spentAmount the spent amount to set
     */
    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    /**
     * Gets the total budget amount.
     *
     * @return the total budget amount
     */
    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the total budget amount.
     *
     * @param totalAmount the total budget amount to set
     */
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Gets the percentage used.
     *
     * @return the percentage used
     */
    public double getPercentage() {
        return percentage;
    }

    /**
     * Sets the percentage used.
     *
     * @param percentage the percentage used to set
     */
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    /**
     * Gets the timestamp when warning was created.
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the timestamp to set in milliseconds
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
