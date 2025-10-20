package com.example.sprintproject.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private TextView totalSpentText;
    private TextView totalRemainingText;
    private LinearLayout categoriesContainer;
    private FirebaseFirestore db;
    private double totalSpent = 0.0;
    private double totalBudget = 0.0;

    // Calendar, Timer
    private TextView timeDisplay;
    private TextView selectedDateDisplay;
    private final Handler handler = new Handler();
    private Runnable updateTimeRunnable;

    private TimeViewModel timeViewModel;


    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        timeViewModel = new ViewModelProvider(requireActivity()).get(TimeViewModel.class);

        db = FirebaseFirestore.getInstance();
        totalSpentText = view.findViewById(R.id.text_total_spent);
        totalRemainingText = view.findViewById(R.id.text_total_remaining);
        categoriesContainer = view.findViewById(R.id.categories_container);

        // Calendar, Timer
        timeDisplay = view.findViewById(R.id.time_display);
        ImageView calendarIcon = view.findViewById(R.id.calendar_icon);
        Button logoutButton = view.findViewById(R.id.logout_button);

        selectedDateDisplay = new TextView(getContext());
        selectedDateDisplay.setTextSize(16);
        selectedDateDisplay.setTextColor(getResources().getColor(R.color.black));
        ((LinearLayout) view.findViewById(R.id.top_bar)).addView(selectedDateDisplay, 0);

        timeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt =
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            selectedDateDisplay.setText("Date: " + fmt.format(date));
            loadDashboardData();
        });

        // Calendar
        calendarIcon.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(selectedYear, selectedMonth, selectedDay);
                        Date selectedDate = selectedCal.getTime();
                        timeViewModel.setCurrentDate(selectedDate);

                        SimpleDateFormat fmt =
                                new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        Toast.makeText(getContext(),
                                "Current date set to: " + fmt.format(selectedDate),
                                Toast.LENGTH_SHORT).show();
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Timer
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat fmt =
                        new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                timeDisplay.setText(fmt.format(new Date()));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);

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
                if (amount == null) {
                    amount = 0.0;
                }
                if (category == null) {
                    category = "Uncategorized";
                }
                totalSpent += amount;
                categoryTotals.put(category,
                        categoryTotals.getOrDefault(category, 0.0) + amount);
            }
            totalSpentText.setText("Total Spent This Period: $" + totalSpent);
            db.collection("budgets").get().addOnSuccessListener(budgetSnapshot -> {
                double totalBudgetLocal = 0.0;
                for (QueryDocumentSnapshot budgetDoc : budgetSnapshot) {
                    Double amt = budgetDoc.getDouble("amount");
                    if (amt != null) {
                        totalBudgetLocal += amt;
                    }
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
