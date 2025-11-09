package com.example.sprintproject.strategy;

import com.example.sprintproject.model.CircleContribution;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Strategy that weights contributions by time remaining.
 * More recent contributions are weighted higher.
 */
public class TimeWeightedStrategy implements ProgressCalculationStrategy {

    private final Date challengeEndDate;

    public TimeWeightedStrategy(Date challengeEndDate) {
        this.challengeEndDate = challengeEndDate;
    }

    @Override
    public double calculateProgress(List<CircleContribution> contributions) {
        if (contributions == null || contributions.isEmpty()) {
            return 0.0;
        }

        Date now = new Date();
        if (challengeEndDate == null || now.after(challengeEndDate)) {
            // If challenge ended, just sum contributions
            return new SumContributionStrategy().calculateProgress(contributions);
        }

        long totalTime = challengeEndDate.getTime() - now.getTime();
        if (totalTime <= 0) {
            return 0.0;
        }

        double weightedTotal = 0.0;
        for (CircleContribution contribution : contributions) {
            if (contribution != null && contribution.getAmount() > 0) {
                Date contribDate = contribution.getDate();
                if (contribDate != null && !contribDate.after(now)) {
                    // Weight: more recent = higher weight
                    long timeSinceContrib = now.getTime() - contribDate.getTime();
                    double weight = 1.0 + (timeSinceContrib / (double) totalTime) * 0.5; // 1.0 to 1.5
                    weightedTotal += contribution.getAmount() * weight;
                }
            }
        }
        return weightedTotal;
    }
}

