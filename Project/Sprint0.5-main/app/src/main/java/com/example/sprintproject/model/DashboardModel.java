package com.example.sprintproject.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DashboardModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Map<String, Object>> getDashboardData() {
        MutableLiveData<Map<String, Object>> liveData = new MutableLiveData<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("DashboardModel", "User not logged in");
            return liveData;
        }

        String userId = user.getUid();
        AtomicReference<Double> totalSpentRef = new AtomicReference<>(0.0);
        Map<String, Double> categoryTotals = new HashMap<>();

        // ✅ Just get all expenses for this user, no date filtering
        db.collection("expenses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(expenseSnapshot -> {
                    Log.d("DashboardModel", "Documents found: " + expenseSnapshot.size());

                    for (QueryDocumentSnapshot doc : expenseSnapshot) {
                        Double amount = doc.getDouble("amount");
                        String category = doc.getString("category");

                        if (amount == null) amount = 0.0;
                        if (category == null || category.isEmpty()) category = "Uncategorized";

                        totalSpentRef.set(totalSpentRef.get() + amount);
                        categoryTotals.put(category,
                                categoryTotals.getOrDefault(category, 0.0) + amount);
                    }

                    // ✅ Combine everything into one map
                    Map<String, Object> data = new HashMap<>();
                    data.put("totalSpent", totalSpentRef.get());
                    data.put("categories", new HashMap<>(categoryTotals));

                    // Optional: If you still have budgets
                    fetchBudgets(userId, data, liveData);
                })
                .addOnFailureListener(e -> Log.e("DashboardModel", "Failed to fetch expenses", e));

        return liveData;
    }

    private void fetchBudgets(String userId, Map<String, Object> existingData,
                              MutableLiveData<Map<String, Object>> liveData) {
        db.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(budgetSnapshot -> {
                    double totalBudget = 0.0;
                    for (QueryDocumentSnapshot budgetDoc : budgetSnapshot) {
                        Double amt = budgetDoc.getDouble("amount");
                        if (amt != null) totalBudget += amt;
                    }

                    existingData.put("totalBudget", totalBudget);
                    liveData.postValue(existingData);
                })
                .addOnFailureListener(e -> Log.e("DashboardModel", "Failed to fetch budgets", e));
    }
}
