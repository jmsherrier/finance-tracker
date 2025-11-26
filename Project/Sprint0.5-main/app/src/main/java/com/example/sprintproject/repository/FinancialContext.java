package com.example.sprintproject.repository;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.SavingsCircle;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Helper class to aggregate user's financial data for AI context.
 * Provides structured financial information for personalized AI responses.
 */
public class FinancialContext {
    private List<Expense> recentExpenses;
    private Map<String, Double> expensesByCategory;
    private double totalExpenses;
    private List<Budget> activeBudgets;
    private Map<String, Double> budgetUtilization; // category -> utilization percentage
    private List<SavingsCircle> activeSavingsCircles;
    private double totalSavingsGoals;
    private Date periodStart;
    private Date periodEnd;

    /**
     * Default constructor.
     * Initializes an empty financial context.
     */
    public FinancialContext() {
        // Constructor intentionally empty - fields are initialized via setters
    }

    /**
     * Gets recent expenses.
     *
     * @return list of recent expenses
     */
    public List<Expense> getRecentExpenses() {
        return recentExpenses;
    }

    /**
     * Sets recent expenses.
     *
     * @param recentExpenses the expenses
     */
    public void setRecentExpenses(List<Expense> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }

    /**
     * Gets expenses grouped by category.
     *
     * @return map of category to total amount
     */
    public Map<String, Double> getExpensesByCategory() {
        return expensesByCategory;
    }

    /**
     * Sets expenses by category.
     *
     * @param expensesByCategory the category map
     */
    public void setExpensesByCategory(Map<String, Double> expensesByCategory) {
        this.expensesByCategory = expensesByCategory;
    }

    /**
     * Gets total expenses.
     *
     * @return total amount
     */
    public double getTotalExpenses() {
        return totalExpenses;
    }

    /**
     * Sets total expenses.
     *
     * @param totalExpenses the total
     */
    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    /**
     * Gets active budgets.
     *
     * @return list of active budgets
     */
    public List<Budget> getActiveBudgets() {
        return activeBudgets;
    }

    /**
     * Sets active budgets.
     *
     * @param activeBudgets the budgets
     */
    public void setActiveBudgets(List<Budget> activeBudgets) {
        this.activeBudgets = activeBudgets;
    }

    /**
     * Gets budget utilization by category.
     *
     * @return map of category to utilization percentage
     */
    public Map<String, Double> getBudgetUtilization() {
        return budgetUtilization;
    }

    /**
     * Sets budget utilization.
     *
     * @param budgetUtilization the utilization map
     */
    public void setBudgetUtilization(Map<String, Double> budgetUtilization) {
        this.budgetUtilization = budgetUtilization;
    }

    /**
     * Gets active savings circles.
     *
     * @return list of active savings circles
     */
    public List<SavingsCircle> getActiveSavingsCircles() {
        return activeSavingsCircles;
    }

    /**
     * Sets active savings circles.
     *
     * @param activeSavingsCircles the savings circles
     */
    public void setActiveSavingsCircles(List<SavingsCircle> activeSavingsCircles) {
        this.activeSavingsCircles = activeSavingsCircles;
    }

    /**
     * Gets total savings goals.
     *
     * @return total goal amount
     */
    public double getTotalSavingsGoals() {
        return totalSavingsGoals;
    }

    /**
     * Sets total savings goals.
     *
     * @param totalSavingsGoals the total
     */
    public void setTotalSavingsGoals(double totalSavingsGoals) {
        this.totalSavingsGoals = totalSavingsGoals;
    }

    /**
     * Gets period start date.
     *
     * @return start date
     */
    public Date getPeriodStart() {
        return periodStart;
    }

    /**
     * Sets period start date.
     *
     * @param periodStart the start date
     */
    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    /**
     * Gets period end date.
     *
     * @return end date
     */
    public Date getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Sets period end date.
     *
     * @param periodEnd the end date
     */
    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    /**
     * Checks if context has any data.
     *
     * @return true if context has data
     */
    public boolean hasData() {
        return (recentExpenses != null && !recentExpenses.isEmpty())
                || (activeBudgets != null && !activeBudgets.isEmpty())
                || (activeSavingsCircles != null && !activeSavingsCircles.isEmpty());
    }
}

