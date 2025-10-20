package com.example.sprintproject.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DashboardModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Map<String, Object>> getDashboardData(Date selectedDate) {
        MutableLiveData<Map<String, Object>> liveData = new MutableLiveData<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return liveData;
        }

        String userId = user.getUid();

        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();

        AtomicReference<Double> totalSpentRef = new AtomicReference<>(0.0);
        Map<String, Double> categoryTotals = new HashMap<>();

        db.collection("expenses")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThan("timestamp", endDate)
                .get()
                .addOnSuccessListener(expenseSnapshot -> {
                    for (QueryDocumentSnapshot doc : expenseSnapshot) {
                        double amount = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0.0;
                        String category = doc.getString("category") != null ? doc.getString("category") : "Uncategorized";
                        totalSpentRef.set(totalSpentRef.get() + amount);
                        categoryTotals.put(category,
                                categoryTotals.getOrDefault(category, 0.0) + amount);
                    }

                    Log.d("DashboardModel", "Fetched categories: " + categoryTotals);

                    // Fetch budget data
                    db.collection("budgets")
                            .whereEqualTo("userId", userId)
                            .whereGreaterThanOrEqualTo("timestamp", startDate)
                            .whereLessThan("timestamp", endDate)
                            .get()
                            .addOnSuccessListener(budgetSnapshot -> {
                                double totalBudget = 0.0;
                                for (QueryDocumentSnapshot budgetDoc : budgetSnapshot) {
                                    Double amt = budgetDoc.getDouble("amount");
                                    if (amt != null) totalBudget += amt;
                                }

                                Map<String, Object> data = new HashMap<>();
                                data.put("totalSpent", totalSpentRef.get());
                                data.put("totalBudget", totalBudget);
                                data.put("categories", new HashMap<>(categoryTotals));

                                liveData.postValue(data);
                            })
                            .addOnFailureListener(e -> Log.e("DashboardModel", "Failed to fetch budgets", e));
                })
                .addOnFailureListener(e -> Log.e("DashboardModel", "Failed to fetch expenses", e));

        return liveData;
    }
}
