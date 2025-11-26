package com.example.sprintproject.utils;

import android.util.Log;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.SavingsCircle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for aggregating financial data for AI context.
 * Provides methods to summarize expenses, budgets, and savings goals.
 */
public class FinancialDataAggregator {
    
    // Private constructor to prevent instantiation
    private FinancialDataAggregator() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /**
     * Gets spending summary for a date range.
     *
     * @param expenses list of expenses
     * @param startDate start date
     * @param endDate end date
     * @return summary string
     */
    public static String getSpendingSummary(List<Expense> expenses,
                                             Date startDate, Date endDate) {
        if (expenses == null || expenses.isEmpty()) {
            return "No expenses recorded in this period.";
        }

        SpendingData data = calculateSpendingData(expenses, startDate, endDate);
        if (data.count == 0) {
            return "No expenses recorded in this period.";
        }

        return buildSpendingSummaryText(data);
    }

    private static SpendingData calculateSpendingData(List<Expense> expenses, Date startDate, Date endDate) {
        double total = 0.0;
        Map<String, Double> byCategory = new HashMap<>();
        int count = 0;

        for (Expense expense : expenses) {
            if (isExpenseInRange(expense, startDate, endDate)) {
                total += expense.getAmount();
                count++;
                String category = getCategory(expense);
                byCategory.put(category, byCategory.getOrDefault(category, 0.0) + expense.getAmount());
            }
        }

        return new SpendingData(total, byCategory, count);
    }

    private static boolean isExpenseInRange(Expense expense, Date startDate, Date endDate) {
        if (expense == null || expense.getDate() == null) {
            return false;
        }
        Date expenseDate = expense.getDate();
        return (startDate == null || !expenseDate.before(startDate))
                && (endDate == null || !expenseDate.after(endDate));
    }

    private static String getCategory(Expense expense) {
        return expense.getCategory() != null ? expense.getCategory() : "Uncategorized";
    }

    private static String buildSpendingSummaryText(SpendingData data) {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Total spending: $%.2f across %d expenses. ", data.total, data.count));
        summary.append("By category: ");
        
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(data.byCategory.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int maxCategories = Math.min(5, sorted.size());
        for (int i = 0; i < maxCategories; i++) {
            Map.Entry<String, Double> entry = sorted.get(i);
            summary.append(entry.getKey()).append(": $")
                    .append(String.format("%.2f", entry.getValue()));
            if (i < maxCategories - 1) {
                summary.append(", ");
            }
        }

        return summary.toString();
    }

    private static class SpendingData {
        final double total;
        final Map<String, Double> byCategory;
        final int count;

        SpendingData(double total, Map<String, Double> byCategory, int count) {
            this.total = total;
            this.byCategory = byCategory;
            this.count = count;
        }
    }

    /**
     * Gets monthly comparison summary.
     *
     * @param currentMonthExpenses current month expenses
     * @param lastMonthExpenses last month expenses
     * @return comparison string
     */
    public static String getMonthlyComparison(List<Expense> currentMonthExpenses,
                                               List<Expense> lastMonthExpenses) {
        double currentTotal = 0.0;
        double lastTotal = 0.0;

        if (currentMonthExpenses != null) {
            for (Expense e : currentMonthExpenses) {
                if (e != null) {
                    currentTotal += e.getAmount();
                }
            }
        }

        if (lastMonthExpenses != null) {
            for (Expense e : lastMonthExpenses) {
                if (e != null) {
                    lastTotal += e.getAmount();
                }
            }
        }

        double difference = currentTotal - lastTotal;
        double percentChange = lastTotal > 0 ? (difference / lastTotal) * 100 : 0;

        String direction = difference > 0 ? "increased" : "decreased";
        return String.format("This month: $%.2f. Last month: $%.2f. "
                        + "Spending %s by $%.2f (%.1f%%).",
                currentTotal, lastTotal, direction, Math.abs(difference),
                Math.abs(percentChange));
    }

    /**
     * Gets cost-cutting suggestions based on expenses and budgets.
     *
     * @param expenses list of expenses
     * @param budgets list of budgets
     * @return list of suggestions
     */
    public static List<String> getCostCutSuggestions(List<Expense> expenses,
                                                       List<Budget> budgets) {
        List<String> suggestions = new ArrayList<>();

        if (expenses == null || expenses.isEmpty()) {
            suggestions.add("Track your expenses to get personalized savings suggestions.");
            return suggestions;
        }

        Map<String, Double> categorySpending = calculateCategorySpending(expenses);
        addOverBudgetSuggestions(suggestions, budgets, categorySpending);
        addTopSpendingSuggestion(suggestions, categorySpending);

        if (suggestions.isEmpty()) {
            suggestions.add("Your spending looks balanced. Keep tracking to maintain good habits!");
        }

        return suggestions;
    }

    private static Map<String, Double> calculateCategorySpending(List<Expense> expenses) {
        Map<String, Double> categorySpending = new HashMap<>();
        for (Expense expense : expenses) {
            if (expense != null && expense.getCategory() != null) {
                String category = expense.getCategory();
                categorySpending.put(category,
                        categorySpending.getOrDefault(category, 0.0) + expense.getAmount());
            }
        }
        return categorySpending;
    }

    private static void addOverBudgetSuggestions(List<String> suggestions, List<Budget> budgets,
                                                 Map<String, Double> categorySpending) {
        if (budgets == null) {
            return;
        }
        for (Budget budget : budgets) {
            if (budget == null) {
                continue;
            }
            String category = budget.getCategory();
            double spent = categorySpending.getOrDefault(category, 0.0);
            double budgetAmount = budget.getTotalAmount();
            if (spent > budgetAmount && budgetAmount > 0) {
                double over = spent - budgetAmount;
                suggestions.add(String.format("You're over budget in %s by $%.2f. "
                                + "Consider reducing spending in this category.",
                        category, over));
            }
        }
    }

    private static void addTopSpendingSuggestion(List<String> suggestions,
                                                  Map<String, Double> categorySpending) {
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(categorySpending.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        if (!sorted.isEmpty() && sorted.get(0).getValue() > 100) {
            suggestions.add(String.format("Your highest spending category is %s ($%.2f). "
                            + "Look for ways to reduce costs here.",
                    sorted.get(0).getKey(), sorted.get(0).getValue()));
        }
    }

    /**
     * Gets current month start date.
     *
     * @return start of current month
     */
    public static Date getCurrentMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Gets last month start date.
     *
     * @return start of last month
     */
    public static Date getLastMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Gets last month end date.
     *
     * @return end of last month
     */
    public static Date getLastMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * Filters expenses by date range.
     *
     * @param expenses all expenses
     * @param startDate start date
     * @param endDate end date
     * @return filtered expenses
     */
    public static List<Expense> filterExpensesByDate(List<Expense> expenses,
                                                       Date startDate, Date endDate) {
        List<Expense> filtered = new ArrayList<>();
        if (expenses == null) {
            return filtered;
        }

        for (Expense expense : expenses) {
            if (expense == null || expense.getDate() == null) {
                continue;
            }
            Date expenseDate = expense.getDate();
            if ((startDate == null || !expenseDate.before(startDate))
                    && (endDate == null || !expenseDate.after(endDate))) {
                filtered.add(expense);
            }
        }

        return filtered;
    }
}

