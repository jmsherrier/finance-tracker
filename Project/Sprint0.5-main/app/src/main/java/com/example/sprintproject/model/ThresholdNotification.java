package com.example.sprintproject.model;

/**
 * Represents a budget threshold notification, including the budget info,
 * the threshold percentage, and the current spending progress.
 */
public class ThresholdNotification {
    private final String id;
    private final String budgetId;
    private final String budgetTitle;
    private final int thresholdPercent;
    private final double progress;

    public ThresholdNotification(
            String budgetId, String budgetTitle,
            int thresholdPercent, double progress) {
        this.budgetId = budgetId;
        this.budgetTitle = budgetTitle;
        this.thresholdPercent = thresholdPercent;
        this.progress = progress;
        this.id = budgetId + "_" + thresholdPercent;
    }

    public String getId() {
        return id;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public String getBudgetTitle() {
        return budgetTitle;
    }

    public int getThresholdPercent() {
        return thresholdPercent;
    }

    public double getProgress() {
        return progress;
    }
}
