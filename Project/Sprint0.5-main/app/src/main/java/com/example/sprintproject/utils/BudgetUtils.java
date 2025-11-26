package com.example.sprintproject.utils;

import com.example.sprintproject.model.Budget;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for budget-related calculations and operations.
 */
public final class BudgetUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private BudgetUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if an expense date falls within the budget period.
     *
     * @param budget the budget
     * @param expenseDate the expense date
     * @return true if expense is in budget period
     */
    public static boolean isExpenseInBudgetPeriod(Budget budget, Date expenseDate) {
        if (budget == null || budget.getStartDate() == null || expenseDate == null) {
            return false;
        }

        Calendar budgetStart = Calendar.getInstance();
        budgetStart.setTime(budget.getStartDate());

        Calendar budgetEnd = Calendar.getInstance();
        budgetEnd.setTime(budget.getStartDate());

        // Calculate end date based on frequency
        if ("weekly".equalsIgnoreCase(budget.getFrequency())) {
            budgetEnd.add(Calendar.WEEK_OF_YEAR, 1);
        } else if ("monthly".equalsIgnoreCase(budget.getFrequency())) {
            budgetEnd.add(Calendar.MONTH, 1);
        } else {
            // Default to monthly
            budgetEnd.add(Calendar.MONTH, 1);
        }

        Calendar expenseCal = Calendar.getInstance();
        expenseCal.setTime(expenseDate);

        return !expenseCal.before(budgetStart) && expenseCal.before(budgetEnd);
    }

    /**
     * Gets the end date for a budget period.
     *
     * @param budget the budget
     * @return the end date of the budget period
     */
    public static Date getBudgetPeriodEnd(Budget budget) {
        if (budget == null || budget.getStartDate() == null) {
            return null;
        }

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(budget.getStartDate());

        if ("weekly".equalsIgnoreCase(budget.getFrequency())) {
            endDate.add(Calendar.WEEK_OF_YEAR, 1);
        } else {
            endDate.add(Calendar.MONTH, 1);
        }

        return endDate.getTime();
    }

    /**
     * Calculates the percentage of budget used.
     *
     * @param spentAmount the amount spent
     * @param totalAmount the total budget amount
     * @return percentage used (0-100)
     */
    public static double calculatePercentage(double spentAmount, double totalAmount) {
        if (totalAmount <= 0) {
            return 0.0;
        }
        return (spentAmount / totalAmount) * 100.0;
    }

    /**
     * Determines if a warning should be shown based on percentage.
     *
     * @param percentage the percentage used
     * @return true if warning should be shown
     */
    public static boolean shouldShowWarning(double percentage) {
        return percentage >= 80.0 && percentage < 100.0;
    }

    /**
     * Gets the warning level based on percentage.
     *
     * @param percentage the percentage used
     * @return "high" for 90%+, "medium" for 80-90%, "low" otherwise
     */
    public static String getWarningLevel(double percentage) {
        if (percentage >= 90.0) {
            return "high";
        } else if (percentage >= 80.0) {
            return "medium";
        } else {
            return "low";
        }
    }
}
