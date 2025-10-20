package com.example.sprintproject.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.utils.Utils;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private TextView totalSpentText, totalRemainingText, timeDisplay, selectedDateDisplay;
    private LinearLayout categoriesContainer;
    private final Handler handler = new Handler();
    private Runnable updateTimeRunnable;
    private DashboardViewModel dashboardViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        totalSpentText = view.findViewById(R.id.text_total_spent);
        totalRemainingText = view.findViewById(R.id.text_total_remaining);
        categoriesContainer = view.findViewById(R.id.categories_container);
        timeDisplay = view.findViewById(R.id.time_display);
        ImageView calendarIcon = view.findViewById(R.id.calendar_icon);
        Button logoutButton = view.findViewById(R.id.logout_button);

        selectedDateDisplay = new TextView(getContext());
        ((LinearLayout) view.findViewById(R.id.top_bar)).addView(selectedDateDisplay, 0);

        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        if (dashboardViewModel.getCurrentDate().getValue() == null) {
            dashboardViewModel.setCurrentDate(new Date());
        }


        dashboardViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            selectedDateDisplay.setText("Date: " + fmt.format(date));
        });

        dashboardViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;

            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());

            Object spentObj = data.get("totalSpent");
            Object budgetObj = data.get("totalBudget");
            double totalSpent = spentObj instanceof Number ? ((Number) spentObj).doubleValue() : 0.0;
            double totalBudget = budgetObj instanceof Number ? ((Number) budgetObj).doubleValue() : 0.0;
            double remaining = totalBudget - totalSpent;

            totalSpentText.setText("Total Spent This Period: " + currency.format(totalSpent));
            totalRemainingText.setText("Remaining Budget: " + currency.format(remaining));

            categoriesContainer.removeAllViews();

            @SuppressWarnings("unchecked")
            Map<String, Double> categoryTotals = (Map<String, Double>) data.get("categories");

            if (categoryTotals == null || categoryTotals.isEmpty()) {
                TextView empty = new TextView(getContext());
                empty.setText("No categories found for this period.");
                empty.setTextSize(16);
                categoriesContainer.addView(empty);
                return;
            }

            categoryTotals.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(entry -> {
                        TextView tv = new TextView(getContext());
                        tv.setText(entry.getKey() + ": " + currency.format(entry.getValue()));
                        tv.setTextSize(16);
                        tv.setPadding(0, 4, 0, 4);
                        categoriesContainer.addView(tv);
                    });
        });

        // Calendar Picker
        calendarIcon.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
                    (view1, y, m, d) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(y, m, d, 0, 0, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        dashboardViewModel.setCurrentDate(c.getTime());
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Timer
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                timeDisplay.setText(fmt.format(new Date()));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);

        // Logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Utils.clearCache(requireContext());
            requireActivity().getViewModelStore().clear();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            Toast.makeText(getContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
