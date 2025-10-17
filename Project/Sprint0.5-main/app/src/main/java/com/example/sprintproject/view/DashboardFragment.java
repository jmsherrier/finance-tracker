package com.example.sprintproject.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private TextView tvTotalSpent, tvTotalRemaining;
    private RecyclerView recyclerCategorySummary;
    private CategoryAdapter adapter;
    private FirebaseFirestore db;

    private Map<String, Double> categorySpending = new HashMap<>();
    private Map<String, Double> categoryBudgets = new HashMap<>();


    public DashboardFragment() {
        // Required empty public constructor

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard, container, false);

        tvTotalSpent - view.findById(R.id.tvTotalSpent);
        tvTotalRemaining = view.findViewById(R.id.tvTotalRemaining);
        recyclerCategorySummary = view.findVieyById(R.id.recyclerCategorySummary);
        recyclerCategorySummary.setLayoutManager((new LinearLayoutManager(getContext())));

        db = FirebaseFirestore.getInstance();

        loadDashboardData();
        return view;
    }

    private void loadDashboardData() {
        db.collection("budgets").get().addOnSuccessListener(budgetSnapshots -> {
            for (QueryDocumentSnapshot doc : budgetSnapshots) {
                String category = doc.getString("category");
                Double limit = doc.getDouble("limit");
                if (category != null && limit != null) {
                    categoryBudgets.put(category, limit);
                }
            }
            db.collection("expenses").get().addOnSuccessListener(expenseSnapshots -> {
                for (QueryDocumentSnapshot doc : expenseSnapshots) {
                    String category = doc.getString("category");
                    Double amount = doc.getDouble("amount");
                    if (category != null && amount != null) {
                        categoryBudgets.put(category, categorySpending.getOrDefault(category, 0.0) + amount);
                    }
                }
                updateDashboard();
            }).addOnFailureListener(e -> Log.e("Firestore", "Error loading expenses", e));
        }).addOnFailureListener(e -> Log.e("Firesotore", "Error loading budgets", e));
    }

    private void updateDashboard() {
        List<CategorySummary> categoryList = new ArrayList<>();
        double totalSpent = 0;
        double totalBudget = 0;

        for (String category : categoryBudgets.keySet()) {
            double budget = categoryBudgets.get(category);
            double spent = categorySpending.getOrDefault(category, 0.0);
            totalSpent += spent;
            totalBudget += budget;
            categoryList.add(new CategorySummary(category, spent, budget));
        }

        double totalRemaining = totalBudget - totalSpent;
        tvTotalSpent.setText(String.format("$%.2f", totalSpent));
        tvTotalRemaining.setText(String.format("$%.2f", totalRemaining));

        adapter = new CategoryAdapter(categoryList);
        recyclerCategorySummary.setAdapter(adapter);
    }
}
