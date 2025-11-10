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
     * Parameter holder for creating a savings circle.
     * Reduces parameter count for Checkstyle compliance.
     */
    public static class CircleParams {
        private String frequency;
        private String groupName;
        private String creatorEmail;
        private String creatorId;
        private String challengeTitle;
        private double goalAmount;
        private Date startDate;
        private String notes;

        public CircleParams setFrequency(String frequency) {
            this.frequency = frequency;
            return this;
        }

        public CircleParams setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public CircleParams setCreatorEmail(String creatorEmail) {
            this.creatorEmail = creatorEmail;
            return this;
        }

        public CircleParams setCreatorId(String creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public CircleParams setChallengeTitle(String challengeTitle) {
            this.challengeTitle = challengeTitle;
            return this;
        }

        public CircleParams setGoalAmount(double goalAmount) {
            this.goalAmount = goalAmount;
            return this;
        }

        public CircleParams setStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public CircleParams setNotes(String notes) {
            this.notes = notes;
            return this;
        }
    }

    // Create a weekly savings circle.
    public static SavingsCircle createWeeklyCircle(String groupName,
                                                   String creatorEmail,
                                                   String creatorId,
                                                   String challengeTitle,
                                                   double goalAmount,
                                                   Date startDate) {
        return new SavingsCircle(groupName, creatorEmail, creatorId,
                challengeTitle, goalAmount, "weekly", startDate);
    }

    // Create a monthly savings circle.
    public static SavingsCircle createMonthlyCircle(String groupName,
                                                    String creatorEmail,
                                                    String creatorId,
                                                    String challengeTitle,
                                                    double goalAmount,
                                                    Date startDate) {
        return new SavingsCircle(groupName, creatorEmail, creatorId,
                challengeTitle, goalAmount, "monthly", startDate);
    }

    /**
     * Factory method that creates a circle based on frequency.
     * Uses CircleParams to stay under 7 parameters.
     *
     * @param params CircleParams object containing all attributes
     * @return A new SavingsCircle
     */
    public static SavingsCircle createCircle(CircleParams params) {
        SavingsCircle circle;

        if ("weekly".equalsIgnoreCase(params.frequency)) {
            circle = createWeeklyCircle(params.groupName, params.creatorEmail,
                    params.creatorId, params.challengeTitle,
                    params.goalAmount, params.startDate);
        } else if ("monthly".equalsIgnoreCase(params.frequency)) {
            circle = createMonthlyCircle(params.groupName, params.creatorEmail,
                    params.creatorId, params.challengeTitle,
                    params.goalAmount, params.startDate);
        } else {
            throw new IllegalArgumentException("Invalid frequency: "
                    + params.frequency + ". Must be 'weekly' or 'monthly'");
        }

        if (params.notes != null && !params.notes.isEmpty()) {
            circle.withNotes(params.notes);
        }

        return circle;
    }
}

