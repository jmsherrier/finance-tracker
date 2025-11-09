package com.example.sprintproject.strategy;

import java.util.List;

import com.example.sprintproject.model.CircleContribution;

/**
 * Strategy interface for calculating progress in different ways.
 */
public interface ProgressCalculationStrategy {
    /**
     * Calculate progress based on contributions.
     * 
     * @param contributions List of contributions
     * @return Total progress amount
     */
    double calculateProgress(List<CircleContribution> contributions);
}

