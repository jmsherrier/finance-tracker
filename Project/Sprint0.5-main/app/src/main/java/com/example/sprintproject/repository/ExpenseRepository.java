package com.example.sprintproject.repository;

import androidx.annotation.NonNull;

import com.example.sprintproject.manager.BudgetWarningManager;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.BudgetWarning;
import com.example.sprintproject.model.Expense;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;

/**
 * Repository class for managing expense operations.
 * Handles expense creation and budget warning logic.
 * Implements Singleton pattern for centralized expense state management.
 */
public class ExpenseRepository {
    private static final String COLLECTION_EXPENSES = "expenses";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_BUDGETS = "budgets";
    private static final double WARNING_THRESHOLD_80 = 80.0;
    private static final double WARNING_THRESHOLD_90 = 90.0;
    private static final double BUDGET_MAX_THRESHOLD = 100.0;

    private static ExpenseRepository instance;
    private final FirebaseFirestore db;
    private final BudgetWarningManager warningManager;

    /**
     * Private constructor for Singleton pattern.
     *
     * @param db Firebase Firestore instance
     * @param warningManager BudgetWarningManager instance
     */
    private ExpenseRepository(FirebaseFirestore db,
                              BudgetWarningManager warningManager) {
        this.db = db;
        this.warningManager = warningManager;
    }

    /**
     * Gets the singleton instance of ExpenseRepository.
     *
     * @param db Firebase Firestore instance
     * @param warningManager BudgetWarningManager instance
     * @return the singleton instance
     */
    public static synchronized ExpenseRepository getInstance(
            FirebaseFirestore db,
            BudgetWarningManager warningManager) {
        if (instance == null) {
            instance = new ExpenseRepository(db, warningManager);
        }
        return instance;
    }

    /**
     * Adds a new expense and checks for budget warnings.
     *
     * @param expense the expense to add
     * @param onSuccess callback for success
     * @param onFailure callback for failure
     */
    public void addExpense(@NonNull Expense expense,
                           @NonNull OnSuccessListener<DocumentReference> onSuccess,
                           @NonNull OnFailureListener onFailure) {
        // Validate expense
        if (expense == null || expense.getAmount() <= 0
                || expense.getUserId() == null) {
            onFailure.onFailure(
                    new IllegalArgumentException("Invalid expense data"));
            return;
        }

        // Save expense to Firestore
        db.collection(COLLECTION_EXPENSES)
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    expense.setId(documentReference.getId());
                    
                    // Check budgets for warnings
                    checkBudgetWarnings(expense);
                    
                    // Call success callback
                    onSuccess.onSuccess(documentReference);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Checks if any budgets need warnings after adding an expense.
     *
     * @param expense the newly added expense
     */
    private void checkBudgetWarnings(@NonNull Expense expense) {
        String userId = expense.getUserId();
        String category = expense.getCategory();

        if (userId == null || category == null) {
            return;
        }

        // Query budgets for this user and category
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_BUDGETS)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Budget budget = document.toObject(Budget.class);
                        budget.setId(document.getId());
                        
                        if (shouldCheckBudget(budget, expense)) {
                            calculateAndCheckBudgetUsage(budget, userId);
                        }
                    }
                });
    }

    /**
     * Determines if a budget should be checked for warnings.
     *
     * @param budget the budget to check
     * @param expense the expense that was added
     * @return true if budget should be checked
     */
    private boolean shouldCheckBudget(Budget budget, Expense expense) {
        if (budget == null || budget.getTotalAmount() <= 0) {
            return false;
        }

        // Check if expense date falls within budget period
        return isExpenseInBudgetPeriod(budget, expense.getDate());
    }

    /**
     * Checks if an expense date falls within the budget period.
     *
     * @param budget the budget
     * @param expenseDate the expense date
     * @return true if expense is in budget period
     */
    private boolean isExpenseInBudgetPeriod(Budget budget, Date expenseDate) {
        if (budget.getStartDate() == null || expenseDate == null) {
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
     * Calculates budget usage and creates warning if needed.
     *
     * @param budget the budget to check
     * @param userId the user ID
     */
    private void calculateAndCheckBudgetUsage(Budget budget, String userId) {
        String category = budget.getCategory();
        Date startDate = budget.getStartDate();

        if (category == null || startDate == null) {
            return;
        }

        // Calculate date range for budget period
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(startDate);

        if ("weekly".equalsIgnoreCase(budget.getFrequency())) {
            endCal.add(Calendar.WEEK_OF_YEAR, 1);
        } else {
            endCal.add(Calendar.MONTH, 1);
        }

        // Query expenses in this budget period
        db.collection(COLLECTION_EXPENSES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .whereGreaterThanOrEqualTo("date", startCal.getTime())
                .whereLessThan("date", endCal.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSpent = 0.0;
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Expense exp = doc.toObject(Expense.class);
                        if (exp != null && exp.getAmount() > 0) {
                            totalSpent += exp.getAmount();
                        }
                    }

                    // Calculate percentage
                    double percentage = 0.0;
                    if (budget.getTotalAmount() > 0) {
                        percentage = (totalSpent / budget.getTotalAmount())
                                * BUDGET_MAX_THRESHOLD;
                    }

                    // Check if warning needed
                    if (percentage >= WARNING_THRESHOLD_80
                            && percentage < BUDGET_MAX_THRESHOLD) {
                        createBudgetWarning(budget, totalSpent, percentage);
                    }
                });
    }

    /**
     * Creates and queues a budget warning.
     *
     * @param budget the budget
     * @param spentAmount the amount spent
     * @param percentage the percentage used
     */
    private void createBudgetWarning(Budget budget,
                                     double spentAmount,
                                     double percentage) {
        BudgetWarning warning = new BudgetWarning(
                budget.getId(),
                budget.getCategory(),
                spentAmount,
                budget.getTotalAmount(),
                percentage
        );

        warningManager.addWarning(warning);
    }
}
