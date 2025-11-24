package com.example.sprintproject.model;

public class ThresholdNotification {
    public final String id;
    public final String budgetId;
    public final String budgetTitle;
    public final int thresholdPercent;
    public final double progress;

    public ThresholdNotification(String budgetId, String budgetTitle, int thresholdPercent, double progress) {
        this.budgetId = budgetId;
        this.budgetTitle = budgetTitle;
        this.thresholdPercent = thresholdPercent;
        this.progress = progress;
        this.id = budgetId + "_" + thresholdPercent;
    }
}
