package com.example.sprintproject.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.model.Budget;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public final class BudgetRepository {
    private static BudgetRepository instance;
    private final FirebaseFirestore db;
    
    private BudgetRepository() {
        this.db = FirestoreManager.getInstance().getDb();
    }
    
    public static synchronized BudgetRepository getInstance() {
        if (instance == null) {
            instance = new BudgetRepository();
        }
        return instance;
    }
    
    public LiveData<List<Budget>> getBudgets(String userId) {
        MutableLiveData<List<Budget>> budgetsLiveData = new MutableLiveData<>();
        
        db.collection("users").document(userId).collection("budgets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) {
                    budgetsLiveData.setValue(new ArrayList<>());
                    return;
                }
                
                List<Budget> budgets = new ArrayList<>();
                snapshots.forEach(doc -> {
                    Budget budget = doc.toObject(Budget.class);
                    budget.setId(doc.getId());
                    budgets.add(budget);
                });
                
                budgetsLiveData.setValue(budgets);
            });
        
        return budgetsLiveData;
    }
    
    public LiveData<Budget> getBudget(String userId, String budgetId) {
        MutableLiveData<Budget> budgetLiveData = new MutableLiveData<>();
        
        db.collection("users").document(userId)
            .collection("budgets").document(budgetId)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null || !snapshot.exists()) {
                    budgetLiveData.setValue(null);
                    return;
                }
                
                Budget budget = snapshot.toObject(Budget.class);
                if (budget != null) {
                    budget.setId(snapshot.getId());
                    budgetLiveData.setValue(budget);
                }
            });
        
        return budgetLiveData;
    }
    
    public void saveBudget(Budget budget, String userId, OnCompleteListener listener) {
        db.collection("users").document(userId).collection("budgets")
            .add(budget)
            .addOnSuccessListener(ref -> {
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
    
    public void updateBudget(String userId, String budgetId,
                            double totalAmount, double spentAmount,
                            OnCompleteListener listener) {
        db.collection("users").document(userId)
            .collection("budgets").document(budgetId)
            .update("totalAmount", totalAmount, "spentAmount", spentAmount)
            .addOnSuccessListener(v -> {
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
    
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
