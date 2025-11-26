package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BudgetViewModel extends ViewModel {
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private MutableLiveData<List<Budget>> budgetsLiveData = new MutableLiveData<>();
    private MutableLiveData<Budget> currentBudgetLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Expense>> budgetExpensesLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();
    
    public BudgetViewModel() {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        db = firestoreManager.getDb();
        auth = firestoreManager.getAuth();
    }
    
    // LiveData getters
    public LiveData<List<Budget>> getBudgets() {
        return budgetsLiveData;
    }
    
    public LiveData<Budget> getCurrentBudget() {
        return currentBudgetLiveData;
    }
    
    public LiveData<List<Expense>> getBudgetExpenses() {
        return budgetExpensesLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessageLiveData;
    }
    
    // Load all budgets for current user
    public void loadBudgets() {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Budget> budgets = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Budget budget = document.toObject(Budget.class);
                    if (budget != null) {
                        budget.setId(document.getId());
                        budgets.add(budget);
                    }
                }
                
                // Sort by start date (newest first)
                Collections.sort(budgets, (b1, b2) -> b2.getStartDate().compareTo(b1.getStartDate()));
                
                budgetsLiveData.setValue(budgets);
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading budgets: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Load specific budget by ID
    public void loadBudget(String budgetId) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(budgetId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Budget budget = documentSnapshot.toObject(Budget.class);
                    if (budget != null) {
                        budget.setId(documentSnapshot.getId());
                        currentBudgetLiveData.setValue(budget);
                        loadBudgetExpenses(budget);
                    }
                } else {
                    errorLiveData.setValue("Budget not found");
                }
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading budget: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Load expenses for a specific budget
    public void loadBudgetExpenses(Budget budget) {
        String userId = getCurrentUserId();
        Date startDate = budget.getStartDate();
        Date endDate = getEndDate(budget);
        
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereEqualTo("category", budget.getCategory())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Expense> expenses = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Expense expense = document.toObject(Expense.class);
                    if (expense != null) {
                        expense.setId(document.getId());
                        // Filter by date range
                        Date expenseDate = expense.getDate();
                        if (!expenseDate.before(startDate) && !expenseDate.after(endDate)) {
                            expenses.add(expense);
                        }
                    }
                }
                
                Collections.sort(expenses, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                budgetExpensesLiveData.setValue(expenses);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading budget expenses: " + e.getMessage());
            });
    }
    
    // Save new budget
    public void saveBudget(Budget budget) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        budget.setUserId(userId);
        
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .add(budget)
            .addOnSuccessListener(documentReference -> {
                budget.setId(documentReference.getId());
                successMessageLiveData.setValue("Budget saved successfully!");
                loadBudgets(); // Refresh the list
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error saving budget: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Update budget
    public void updateBudget(Budget budget) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(budget.getId())
            .set(budget)
            .addOnSuccessListener(aVoid -> {
                successMessageLiveData.setValue("Budget updated successfully!");
                loadBudgets(); // Refresh the list
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error updating budget: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Delete budget
    public void deleteBudget(String budgetId) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection("users")
            .document(userId)
            .collection("budgets")
            .document(budgetId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                successMessageLiveData.setValue("Budget deleted successfully!");
                loadBudgets(); // Refresh the list
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error deleting budget: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Calculate budget utilization
    public double calculateBudgetUtilization(Budget budget, List<Expense> expenses) {
        double totalSpent = 0;
        Date startDate = budget.getStartDate();
        Date endDate = getEndDate(budget);
        
        for (Expense expense : expenses) {
            Date expenseDate = expense.getDate();
            if (!expenseDate.before(startDate) && !expenseDate.after(endDate)) {
                totalSpent += expense.getAmount();
            }
        }
        
        return totalSpent;
    }
    
    // Get budget end date based on frequency
    private Date getEndDate(Budget budget) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(budget.getStartDate());
        
        if ("weekly".equalsIgnoreCase(budget.getFrequency())) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        } else {
            calendar.add(Calendar.MONTH, 1);
        }
        
        return calendar.getTime();
    }
    
    // Clear error and success messages
    public void clearMessages() {
        errorLiveData.setValue(null);
        successMessageLiveData.setValue(null);
    }
    
    private String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }
}
