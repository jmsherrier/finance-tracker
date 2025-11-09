package com.example.sprintproject.strategy;

import com.example.sprintproject.model.CircleContribution;

import java.util.List;

/**
 * Strategy that calculates progress by simply summing all contributions.
 */
public class SumContributionStrategy implements ProgressCalculationStrategy {

    @Override
    public double calculateProgress(List<CircleContribution> contributions) {
        if (contributions == null || contributions.isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (CircleContribution contribution : contributions) {
            if (contribution != null && contribution.getAmount() > 0) {
                total += contribution.getAmount();
            }
        }
        return total;
    }
}

