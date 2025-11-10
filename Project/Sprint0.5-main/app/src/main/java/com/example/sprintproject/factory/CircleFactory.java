package com.example.sprintproject.factory;

import com.example.sprintproject.model.SavingsCircle;

import java.util.Date;

/**
 * Factory pattern for creating different types of Savings Circles.
 * Handles creation logic for weekly and monthly circles.
 */
public final class CircleFactory {

    private CircleFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create a weekly savings circle.
     * @param groupName The name of the group
     * @param creatorEmail The email of the circle creator
     * @param creatorId The ID of the circle creator
     * @param challengeTitle The title of the savings challenge
     * @param goalAmount The savings goal amount
     * @param startDate The start date of the circle
     * @param notes Additional notes for the circle
     * @return A new weekly SavingsCircle
     */
    public static SavingsCircle createWeeklyCircle(String groupName,
                                                   String creatorEmail,
                                                   String creatorId,
                                                   String challengeTitle,
                                                   double goalAmount,
                                                   Date startDate,
                                                   String notes) {
        SavingsCircle circle = new SavingsCircle(groupName, creatorEmail, creatorId,
            challengeTitle, goalAmount, "weekly", startDate, notes);
        return circle;
    }

    /**
     * Create a monthly savings circle.
     * @param groupName The name of the group
     * @param creatorEmail The email of the circle creator
     * @param creatorId The ID of the circle creator
     * @param challengeTitle The title of the savings challenge
     * @param goalAmount The savings goal amount
     * @param startDate The start date of the circle
     * @param notes Additional notes for the circle
     * @return A new monthly SavingsCircle
     */
    public static SavingsCircle createMonthlyCircle(String groupName,
                                                    String creatorEmail,
                                                    String creatorId,
                                                    String challengeTitle,
                                                    double goalAmount,
                                                    Date startDate,
                                                    String notes) {
        SavingsCircle circle = new SavingsCircle(groupName, creatorEmail, creatorId,
            challengeTitle, goalAmount, "monthly", startDate, notes);
        return circle;
    }

    /**
     * Factory method that creates a circle based on frequency string.
     * @param frequency The frequency ("weekly" or "monthly")
     * @param groupName The name of the group
     * @param creatorEmail The email of the circle creator
     * @param creatorId The ID of the circle creator
     * @param challengeTitle The title of the savings challenge
     * @param goalAmount The savings goal amount
     * @param startDate The start date of the circle
     * @param notes Additional notes for the circle
     * @return A new SavingsCircle with the specified frequency
     */
    public static SavingsCircle createCircle(String frequency, String groupName,
                                             String creatorEmail, String creatorId,
                                             String challengeTitle, double goalAmount,
                                             Date startDate, String notes) {
        if ("weekly".equalsIgnoreCase(frequency)) {
            return createWeeklyCircle(groupName, creatorEmail, creatorId,
                challengeTitle, goalAmount, startDate, notes);
        } else if ("monthly".equalsIgnoreCase(frequency)) {
            return createMonthlyCircle(groupName, creatorEmail, creatorId,
                challengeTitle, goalAmount, startDate, notes);
        } else {
            throw new IllegalArgumentException("Invalid frequency: " + frequency
                + ". Must be 'weekly' or 'monthly'");
        }
    }
}

