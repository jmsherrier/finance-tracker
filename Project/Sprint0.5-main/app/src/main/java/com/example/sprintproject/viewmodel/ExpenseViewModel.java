package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpenseViewModel extends ViewModel {
    
    // Constants for Firestore collections
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_EXPENSES = "expenses";
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();
    
    public ExpenseViewModel() {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        db = firestoreManager.getDb();
        auth = firestoreManager.getAuth();
    }
    
    // LiveData getters
    public LiveData<List<Expense>> getExpenses() {
        return expensesLiveData;
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
    
    // Load all expenses for current user
    public void loadExpenses() {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_EXPENSES)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Expense> expenses = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Expense expense = document.toObject(Expense.class);
                    if (expense != null) {
                        expense.setId(document.getId());
                        expenses.add(expense);
                    }
                }
                
                // Sort by date (newest first)
                Collections.sort(expenses, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                
                expensesLiveData.setValue(expenses);
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading expenses: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Save new expense
    public void saveExpense(Expense expense) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        expense.setUserId(userId);
        
        db.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_EXPENSES)
            .add(expense)
            .addOnSuccessListener(documentReference -> {
                expense.setId(documentReference.getId());
                successMessageLiveData.setValue("Expense saved successfully!");
                loadExpenses(); // Refresh the list
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error saving expense: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Delete expense
    public void deleteExpense(String expenseId) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_EXPENSES)
            .document(expenseId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                successMessageLiveData.setValue("Expense deleted successfully!");
                loadExpenses(); // Refresh the list
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error deleting expense: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Update expense
    public void updateExpense(Expense expense) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_EXPENSES)
            .document(expense.getId())
            .set(expense)
            .addOnSuccessListener(aVoid -> {
                successMessageLiveData.setValue("Expense updated successfully!");
                loadExpenses(); // Refresh the list
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error updating expense: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
    }
    
    // Get expenses by category
    public void loadExpensesByCategory(String category) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        
        db.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_EXPENSES)
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Expense> expenses = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Expense expense = document.toObject(Expense.class);
                    if (expense != null) {
                        expense.setId(document.getId());
                        expenses.add(expense);
                    }
                }
                
                Collections.sort(expenses, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                expensesLiveData.setValue(expenses);
                loadingLiveData.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading expenses: " + e.getMessage());
                loadingLiveData.setValue(false);
            });
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
