package com.example.sprintproject.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import com.google.firebase.auth.FirebaseAuth;

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
    private PieChart pieChart;
    private BarChart barChart;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTimeRunnable;

    private DashboardViewModel dashboardViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Views
        totalSpentText = view.findViewById(R.id.text_total_spent);
        totalRemainingText = view.findViewById(R.id.text_total_remaining);
        categoriesContainer = view.findViewById(R.id.categories_container);
        timeDisplay = view.findViewById(R.id.time_display);
        ImageView calendarIcon = view.findViewById(R.id.calendar_icon);
        Button logoutButton = view.findViewById(R.id.logout_button);

        // Add "Date: ..." TextView into top bar (index 0 so it shows on the left)
        LinearLayout topBar = view.findViewById(R.id.top_bar);
        selectedDateDisplay = new TextView(requireContext());
        topBar.addView(selectedDateDisplay, 0);

        // Charts
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("Spending Breakdown");
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);

        // ViewModel
        dashboardViewModel =
                new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Observe date label
        dashboardViewModel.getCurrentDate().observe(
                getViewLifecycleOwner(),
                date -> {
                    if (date == null) date = new Date();
                    SimpleDateFormat fmt =
                            new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    selectedDateDisplay.setText("Date: " + fmt.format(date));
                });

        // Calendar picker
        calendarIcon.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            Date current = dashboardViewModel.getCurrentDate().getValue();
            if (current != null) {
                cal.setTime(current);
            }
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
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

        // Live clock
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

            Toast.makeText(getContext(),
                    "Logged out successfully!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe combined dashboard data once
        dashboardViewModel.getDashboardData().observe(
                getViewLifecycleOwner(),
                data -> {
                    if (data == null) return;

                    // Numbers
                    Double totalSpentObj = (Double) data.get("totalSpent");
                    Double totalBudgetObj = (Double) data.get("totalBudget");
                    double totalSpent = totalSpentObj != null ? totalSpentObj : 0.0;
                    double totalBudget = totalBudgetObj != null ? totalBudgetObj : 0.0;
                    double remaining = Math.max(0.0, totalBudget - totalSpent);

                    totalSpentText.setText(String.format(
                            Locale.getDefault(),
                            "Total Spent This Period: $%.2f",
                            totalSpent
                    ));
                    totalRemainingText.setText(String.format(
                            Locale.getDefault(),
                            "Remaining Budget: $%.2f",
                            remaining
                    ));

                    // Category list
                    categoriesContainer.removeAllViews();
                    Map<String, Double> categories =
                            (Map<String, Double>) data.get("categories");

                    if (categories != null && !categories.isEmpty()) {
                        for (Map.Entry<String, Double> entry : categories.entrySet()) {
                            TextView tv = new TextView(requireContext());
                            tv.setText(String.format(
                                    Locale.getDefault(),
                                    "%s: $%.2f",
                                    entry.getKey(),
                                    entry.getValue()
                            ));
                            tv.setTextSize(16);
                            tv.setPadding(0, 4, 0, 4);
                            categoriesContainer.addView(tv);
                        }
                    } else {
                        TextView tv = new TextView(requireContext());
                        tv.setText("No expenses for this period.");
                        tv.setTextSize(16);
                        tv.setPadding(0, 4, 0, 4);
                        categoriesContainer.addView(tv);
                    }

                    // Update chart data in ViewModel
                    dashboardViewModel.updateCharts(data);
                }
        );

        // Observe Pie chart data
        dashboardViewModel.getPieData().observe(
                getViewLifecycleOwner(),
                pieData -> {
                    if (pieData == null) return;
                    pieChart.setData(pieData);
                    pieChart.invalidate();
                }
        );

        // Observe Bar chart data
        dashboardViewModel.getBarData().observe(
                getViewLifecycleOwner(),
                barData -> {
                    if (barData == null) return;
                    barChart.setData(barData);
                    barChart.invalidate();
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateTimeRunnable);
    }

    /**
     * For unit tests.
     */
    public void resetDashboardData() {
        if (dashboardViewModel != null) {
            dashboardViewModel.clearData();
        }
    }
}