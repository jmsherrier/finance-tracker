package com.example.sprintproject.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
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

        // Define month range
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();

        // Use AtomicReference for mutability inside lambdas
        AtomicReference<Double> totalSpentRef = new AtomicReference<>(0.0);
        Map<String, Double> categoryTotals = new HashMap<>();

        db.collection("expenses")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startDate))
                .whereLessThan("timestamp", new Timestamp(endDate))
                .get()
                .addOnSuccessListener(expenseSnapshot -> {
                    for (QueryDocumentSnapshot doc : expenseSnapshot) {
                        Double amtField = doc.getDouble("amount");
                        double amount = (amtField != null) ? amtField : 0.0;
                        String category = doc.getString("category") != null
                                ? doc.getString("category")
                                : "Uncategorized";

                        totalSpentRef.set(totalSpentRef.get() + amount);
                        categoryTotals.put(
                                category,
                                categoryTotals.getOrDefault(category, 0.0) + amount
                        );
                    }

                    // Now fetch budget data
                    db.collection("budgets")
                            .whereEqualTo("userId", userId)
                            .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startDate))
                            .whereLessThan("timestamp", new Timestamp(endDate))
                            .get()
                            .addOnSuccessListener(budgetSnapshot -> {
                                double totalBudget = 0.0;
                                for (QueryDocumentSnapshot budgetDoc : budgetSnapshot) {
                                    Double amt = budgetDoc.getDouble("amount");
                                    if (amt != null) {
                                        totalBudget += amt;
                                    }
                                }

                                Map<String, Object> data = new HashMap<>();
                                data.put("totalSpent", totalSpentRef.get());
                                data.put("totalBudget", totalBudget);
                                data.put("categories", categoryTotals);
                                liveData.setValue(data);
                            });
                });

        return liveData;
    }
}
