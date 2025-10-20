package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.FirestoreManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DashboardViewModel extends ViewModel {
    private final MutableLiveData<Date> currentDate = new MutableLiveData<>(new Date());
    private final MutableLiveData<Map<String, Object>> dashboardData = new MutableLiveData<>();
    private final FirebaseFirestore db;

    public DashboardViewModel() {
        db = FirestoreManager.getInstance().getDb();
        loadDashboardData();
    }

    public LiveData<Date> getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date date) {
        currentDate.setValue(date);
        loadDashboardData();
    }

    public LiveData<Map<String, Object>> getDashboardData() {
        return dashboardData;
    }

    private void loadDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalSpent", 0.0);
        data.put("totalBudget", 0.0);
        data.put("categories", new HashMap<String, Double>());

        // Load from Firestore
        db.collection("expenses").get().addOnSuccessListener(snapshot -> {
            double total = 0.0;
            Map<String, Double> categories = new HashMap<>();

            snapshot.forEach(doc -> {
                Double amount = doc.getDouble("amount");
                String category = doc.getString("category");
                if (amount != null) {
                    categories.put(category != null ? category : "Other",
                            categories.getOrDefault(category, 0.0) + amount);
                }
            });

            data.put("totalSpent", total);
            data.put("categories", categories);
            dashboardData.setValue(data);
        });
    }
}

