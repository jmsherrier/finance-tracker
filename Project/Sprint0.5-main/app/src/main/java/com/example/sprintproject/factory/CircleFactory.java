package com.example.sprintproject.factory;

import com.example.sprintproject.model.SavingsCircle;

import java.util.Date;

/**
 * Factory pattern for creating different types of Savings Circles.
 * Handles creation logic for weekly and monthly circles.
 */
public class CircleFactory {

    /**
     * Create a weekly savings circle.
     */
    public static SavingsCircle createWeeklyCircle(String groupName, String creatorEmail,
                                                   String creatorId, String challengeTitle,
                                                   double goalAmount, Date startDate, String notes) {
        SavingsCircle circle = new SavingsCircle(
            groupName, creatorEmail, creatorId, challengeTitle, goalAmount, "weekly", startDate, notes
        );
        return circle;
    }

    /**
     * Create a monthly savings circle.
     */
    public static SavingsCircle createMonthlyCircle(String groupName, String creatorEmail,
                                                    String creatorId, String challengeTitle,
                                                    double goalAmount, Date startDate, String notes) {
        SavingsCircle circle = new SavingsCircle(
            groupName, creatorEmail, creatorId, challengeTitle, goalAmount, "monthly", startDate, notes
        );
        return circle;
    }

    /**
     * Factory method that creates a circle based on frequency string.
     */
    public static SavingsCircle createCircle(String frequency, String groupName, String creatorEmail,
                                             String creatorId, String challengeTitle,
                                             double goalAmount, Date startDate, String notes) {
        if ("weekly".equalsIgnoreCase(frequency)) {
            return createWeeklyCircle(groupName, creatorEmail, creatorId, challengeTitle,
                goalAmount, startDate, notes);
        } else if ("monthly".equalsIgnoreCase(frequency)) {
            return createMonthlyCircle(groupName, creatorEmail, creatorId, challengeTitle,
                goalAmount, startDate, notes);
        } else {
            throw new IllegalArgumentException("Invalid frequency: " + frequency 
                + ". Must be 'weekly' or 'monthly'");
        }
    }
}

