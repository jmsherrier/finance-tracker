package com.example.sprintproject.view;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.model.BudgetMonitor;
import com.example.sprintproject.utils.NotificationQueue;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private boolean reminderDialogShown = false;
    private boolean openExpenseLog = false;

    private NotificationQueue queue;
    private BudgetMonitor monitor;
    private static final int[] DEFAULT_THRESHOLDS = new int[] {80, 90};
    private static final String PREFS_KEY = "shownBudgetAlerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);


        DashboardViewModel viewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.checkExpenseReminder();

        viewModel.getExpenseReminder().observe(this, shouldShow -> {
            if (Boolean.TRUE.equals(shouldShow) && !reminderDialogShown) {
                reminderDialogShown = true;
                showExpenseReminderDialog();
            }
        queue = new NotificationQueue((item, onComplete) -> {
                ThresholdDialogFragment frag = ThresholdDialogFragment.newInstance(item);
                frag.setOnComplete(onComplete);
                frag.show(getSupportFragmentManager(), "threshold_" + item.getBudgetId() + "_" + item.getThresholdPercent());
            });
        });
        monitor = new BudgetMonitor(getApplicationContext(), queue, DEFAULT_THRESHOLDS, PREFS_KEY);

        // Load DashboardFragment by default
        if (savedInstanceState == null) {
            Fragment initialFragment = openExpenseLog
                    ? new ExpenseLogFragment()
                    : new DashboardFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, initialFragment)
                    .commit();

            // If opening expense log, also select the nav item
            if (openExpenseLog) {
                bottomNav.setSelectedItemId(R.id.nav_expense);
            }
        }

        // Handle navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                android.widget.Toast.makeText(this, "Dashboard",
                        android.widget.Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_expense) {
                selectedFragment = new ExpenseLogFragment();
                android.widget.Toast.makeText(this, "Expense Log",
                        android.widget.Toast.LENGTH_SHORT).show();

            } else if (item.getItemId() == R.id.nav_budgets) {
                selectedFragment = new BudgetListFragment();
                android.widget.Toast.makeText(this, "Budgets",
                        android.widget.Toast.LENGTH_SHORT).show();

            } else if (item.getItemId() == R.id.nav_chatbot) {
                selectedFragment = new ChatbotFragment();
                android.widget.Toast.makeText(this, "Chatbot",
                        android.widget.Toast.LENGTH_SHORT).show();

            } else if (item.getItemId() == R.id.nav_saving_circles) {
                selectedFragment = new SavingsCirclesFragment();
                android.widget.Toast.makeText(this, "Savings Circles",
                        android.widget.Toast.LENGTH_SHORT).show();

            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    private void showExpenseReminderDialog() {
        new AlertDialog.Builder(this, R.style.ThemeOverlay_SprintProject_AlertDialog)
                .setTitle("Expense Reminder")
                .setMessage("You haven't logged any expenses recently.")
                .setPositiveButton("Go to Expense Log", (d, w) -> {
                    reminderDialogShown = false;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ExpenseLogFragment())
                            .commit();
                })
                .setNegativeButton("Dismiss", (d, w) -> reminderDialogShown = false)
                .show();
    }

}


