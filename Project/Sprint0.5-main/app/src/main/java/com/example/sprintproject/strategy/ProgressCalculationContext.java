package com.example.sprintproject.strategy;

import com.example.sprintproject.model.CircleContribution;
import com.example.sprintproject.model.SavingsCircle;

import java.util.List;

/**
 * Context class that uses a strategy to calculate progress.
 * Demonstrates the Strategy pattern.
 */
public class ProgressCalculationContext {
    private ProgressCalculationStrategy strategy;

    /**
     * Constructor.
     * @param strategy The strategy to use
     */
    public ProgressCalculationContext(ProgressCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Set the strategy to use.
     * @param strategy The strategy to set
     */
    public void setStrategy(ProgressCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Calculate progress using the current strategy.
     * @param contributions The list of contributions
     * @return The calculated progress
     */
    public double calculateProgress(List<CircleContribution> contributions) {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set");
        }
        return strategy.calculateProgress(contributions);
    }

    /**
     * Factory method to create appropriate strategy based on circle type.
     * @param circle The savings circle
     * @param contributions The list of contributions
     * @return The configured context
     */
    public static ProgressCalculationContext createContext(SavingsCircle circle,
            List<CircleContribution> contributions) {
        if (circle == null) {
            return new ProgressCalculationContext(new SumContributionStrategy());
        }

        // For now, use simple sum strategy
        // Could be extended to use time-weighted strategy for active circles
        if (circle.isActive() && circle.getEndDate() != null) {
            return new ProgressCalculationContext(
                new TimeWeightedStrategy(circle.getEndDate())
            );
        } else {
            return new ProgressCalculationContext(new SumContributionStrategy());
        }
    }
}

