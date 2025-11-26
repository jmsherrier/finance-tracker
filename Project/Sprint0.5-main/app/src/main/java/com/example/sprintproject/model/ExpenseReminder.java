package com.example.sprintproject.model;

import java.io.Serializable;

/**
 * Represents a missed expense log reminder notification.
 * Contains information about days since last expense was logged.
 */
public class ExpenseReminder implements Serializable {
    private static final long serialVersionUID = 1L;

    private int daysSinceLastExpense;
    private long lastExpenseTimestamp;
    private String userId;

    /**
     * Default constructor for ExpenseReminder.
     */
    public ExpenseReminder() {
    }

    /**
     * Constructor for creating a new expense reminder.
     *
     * @param daysSinceLastExpense the number of days since last expense
     * @param lastExpenseTimestamp the timestamp of last expense
     * @param userId the user ID
     */
    public ExpenseReminder(int daysSinceLastExpense,
                          long lastExpenseTimestamp,
                          String userId) {
        this.daysSinceLastExpense = daysSinceLastExpense;
        this.lastExpenseTimestamp = lastExpenseTimestamp;
        this.userId = userId;
    }

    /**
     * Gets the days since last expense.
     *
     * @return days since last expense
     */
    public int getDaysSinceLastExpense() {
        return daysSinceLastExpense;
    }

    /**
     * Sets the days since last expense.
     *
     * @param daysSinceLastExpense days to set
     */
    public void setDaysSinceLastExpense(int daysSinceLastExpense) {
        this.daysSinceLastExpense = daysSinceLastExpense;
    }

    /**
     * Gets the last expense timestamp.
     *
     * @return timestamp in milliseconds
     */
    public long getLastExpenseTimestamp() {
        return lastExpenseTimestamp;
    }

    /**
     * Sets the last expense timestamp.
     *
     * @param lastExpenseTimestamp timestamp to set
     */
    public void setLastExpenseTimestamp(long lastExpenseTimestamp) {
        this.lastExpenseTimestamp = lastExpenseTimestamp;
    }

    /**
     * Gets the user ID.
     *
     * @return user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
