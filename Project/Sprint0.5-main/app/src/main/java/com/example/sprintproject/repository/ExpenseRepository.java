package com.example.sprintproject.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.model.Expense;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class ExpenseRepository {
    private static ExpenseRepository instance;
    private final FirebaseFirestore db;
    
    private ExpenseRepository() {
        this.db = FirestoreManager.getInstance().getDb();
    }
    
    public static synchronized ExpenseRepository getInstance() {
        if (instance == null) {
            instance = new ExpenseRepository();
        }
        return instance;
    }
    
    public LiveData<List<Expense>> getExpenses(String userId) {
        MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
        
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) {
                    expensesLiveData.setValue(new ArrayList<>());
                    return;
                }
                
                List<Expense> expenses = new ArrayList<>();
                snapshots.forEach(doc -> {
                    Expense expense = doc.toObject(Expense.class);
                    expense.setId(doc.getId());
                    expenses.add(expense);
                });
                
                // If empty, create seed data
                if (expenses.isEmpty()) {
                    createSeedExpenses(userId, expensesLiveData);
                } else {
                    expensesLiveData.setValue(expenses);
                }
            });
        
        return expensesLiveData;
    }
    
    public void saveExpense(Expense expense, OnCompleteListener listener) {
        db.collection("expenses")
            .add(expense)
            .addOnSuccessListener(ref -> {
                expense.setId(ref.getId());
                if (listener != null) {
                    listener.onSuccess();
                }
            })
            .addOnFailureListener(e -> {
                if (listener != null) {
                    listener.onFailure(e.getMessage());
                }
            });
    }
    
    private void createSeedExpenses(String userId, MutableLiveData<List<Expense>> liveData) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        
        Expense expense1 = new Expense(
            "Grocery Shopping", 85.50, "Food & Dining",
            calendar.getTime(), "Weekly groceries from supermarket", userId
        );
        
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Expense expense2 = new Expense(
            "Gas Fill-up", 45.00, "Transportation",
            calendar.getTime(), "Regular gas at local station", userId
        );
        
        List<Expense> seedExpenses = new ArrayList<>();
        
        db.collection("expenses").add(expense1)
            .addOnSuccessListener(ref1 -> {
                expense1.setId(ref1.getId());
                seedExpenses.add(expense1);
                
                db.collection("expenses").add(expense2)
                    .addOnSuccessListener(ref2 -> {
                        expense2.setId(ref2.getId());
                        seedExpenses.add(expense2);
                        
                        Collections.sort(seedExpenses, (e1, e2) ->
                            e2.getDate().compareTo(e1.getDate()));
                        liveData.setValue(seedExpenses);
                    });
            });
    }
    
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
