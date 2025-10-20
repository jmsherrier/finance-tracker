package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.repository.BudgetRepository;

import java.util.List;

public class BudgetViewModel extends ViewModel {
    private final BudgetRepository repository;
    private LiveData<List<Budget>> budgets;
    private String currentUserId;
    
    public BudgetViewModel() {
        repository = BudgetRepository.getInstance();
    }
    
    public LiveData<List<Budget>> getBudgets(String userId) {
        if (budgets == null || !userId.equals(currentUserId)) {
            currentUserId = userId;
            budgets = repository.getBudgets(userId);
        }
        return budgets;
    }
    
    public LiveData<Budget> getBudget(String userId, String budgetId) {
        return repository.getBudget(userId, budgetId);
    }
    
    public void saveBudget(Budget budget, String userId,
                          BudgetRepository.OnCompleteListener listener) {
        repository.saveBudget(budget, userId, listener);
    }
    
    public void updateBudget(String userId, String budgetId,
                            double totalAmount, double spentAmount,
                            BudgetRepository.OnCompleteListener listener) {
        repository.updateBudget(userId, budgetId, totalAmount, spentAmount, listener);
    }
}
