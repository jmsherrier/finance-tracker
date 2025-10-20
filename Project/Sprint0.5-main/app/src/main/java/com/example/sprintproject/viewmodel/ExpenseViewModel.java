package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Expense;
import com.example.sprintproject.repository.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends ViewModel {
    private final ExpenseRepository repository;
    private LiveData<List<Expense>> expenses;
    private String currentUserId;
    
    public ExpenseViewModel() {
        repository = ExpenseRepository.getInstance();
    }
    
    public LiveData<List<Expense>> getExpenses(String userId) {
        if (expenses == null || !userId.equals(currentUserId)) {
            currentUserId = userId;
            expenses = repository.getExpenses(userId);
        }
        return expenses;
    }
    
    public void saveExpense(Expense expense,
                           ExpenseRepository.OnCompleteListener listener) {
        repository.saveExpense(expense, listener);
    }
}
