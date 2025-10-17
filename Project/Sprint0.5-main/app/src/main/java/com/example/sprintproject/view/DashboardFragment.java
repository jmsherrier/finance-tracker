package com.example.sprintproject.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private TextView totalSpentText, totalRemainingText;
    private LinearLayout categoriesContainer;
    private FirebaseFirestore db;
    private double totalSpent = 0.0;
    private double totalBudget = 0.0;


    public DashboardFragment() {
        // Required empty public constructor

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);


        db = FirebaseFirestore.getInstance();
        totalSpentText = view.findViewById(R.id.text_total_spent);
        totalRemainingText = view.findViewById(R.id.text_total_remaining);
        categoriesContainer = view.findViewById(R.id.categories_container);

        loadDashboardData();
        return view;
    }

    private void loadDashboardData() {
        totalSpent = 0.0;
        db.collection("expenses").get().addOnSuccessListener(querySnapshot -> {
            Map<String, Double> categoryTotals = new HashMap<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Double amount = doc.getDouble("amount");
                String category = doc.getString("category");
                if (amount == null) amount = 0.0;
                if (category == null) category = "Uncategorized";
                totalSpent += amount;
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
            totalSpentText.setText("Total Spent This Period: $" + totalSpent);
            db.collection("budgets").get().addOnSuccessListener(budgetSnapshot -> {
                double totalBudgetLocal = 0.0;
                for (QueryDocumentSnapshot budgetDoc : budgetSnapshot) {
                    Double amt = budgetDoc.getDouble("amount");
                    if (amt != null) totalBudgetLocal += amt;
                }
                double remaining = totalBudgetLocal - totalSpent;
                totalRemainingText.setText("Remaining Budget: $" + remaining);

                categoriesContainer.removeAllViews();
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    TextView tv = new TextView(getContext());
                    tv.setText(entry.getKey() + ": $" + entry.getValue());
                    tv.setTextSize(16);
                    tv.setPadding(0, 4, 0, 4);
                    categoriesContainer.addView(tv);
                }
            });
        });
    }
}
