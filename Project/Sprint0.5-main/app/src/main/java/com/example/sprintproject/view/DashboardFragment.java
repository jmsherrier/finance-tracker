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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private TextView totalSpentText;
    private TextView totalRemainingText;
    private TextView timeDisplay;
    private TextView selectedDateDisplay;
    private LinearLayout categoriesContainer;
    private final Handler handler = new Handler();
    private Runnable updateTimeRunnable;
    private DashboardViewModel dashboardViewModel;

    private PieChart pieChart;
    private BarChart barChart;

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
        LinearLayout topBar = view.findViewById(R.id.top_bar);
        topBar.addView(selectedDateDisplay, 0);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("Spending Breakdown");
        barChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        barChart.setFitBars(true);

        dashboardViewModel =
                new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Observe date changes
        dashboardViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            selectedDateDisplay.setText("Date: " + fmt.format(date));
        });

        // Observe data changes
        dashboardViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data == null) {
                return;
            }
            double totalSpent = (double) data.get("totalSpent");
            double totalBudget = (double) data.get("totalBudget");
            double remaining = totalBudget - totalSpent;

            totalSpentText.setText("Total Spent This Period: $" + totalSpent);
            totalRemainingText.setText("Remaining Budget: $" + remaining);

            categoriesContainer.removeAllViews();
            Map<String, Double> categoryTotals = (Map<String, Double>) data.get("categories");
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                TextView tv = new TextView(getContext());
                tv.setText(entry.getKey() + ": $" + entry.getValue());
                tv.setTextSize(16);
                tv.setPadding(0, 4, 0, 4);
                categoriesContainer.addView(tv);
            }
        });

        // Calendar Picker
        calendarIcon.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
                    (view1, y, m, d) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(y, m, d);
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            Toast.makeText(getContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dashboardViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            dashboardViewModel.updateCharts(data);
        });
        dashboardViewModel.getBarData().observe(getViewLifecycleOwner(), barData -> {
            barChart.setData(barData);
            barChart.invalidate();
        });
        dashboardViewModel.getPieData().observe(getViewLifecycleOwner(), pieData -> {
            pieChart.setData(pieData);
            pieChart.invalidate();
        });
    }
}
