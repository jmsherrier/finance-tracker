package com.example.sprintproject.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.utils.Utils;
import com.example.sprintproject.model.BudgetMonitor;
import com.example.sprintproject.utils.NotificationQueue;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

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
        
        // Apply saved dark mode preference
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        Button logoutButton = findViewById(R.id.logout_button);

        // Setup logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Utils.clearCache(this);
            getViewModelStore().clear();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this,
                    "Logged out successfully!", Toast.LENGTH_SHORT).show();
        });


        DashboardViewModel viewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.checkExpenseReminder();

        viewModel.getExpenseReminder().observe(this, shouldShow -> {
            if (Boolean.TRUE.equals(shouldShow) && !reminderDialogShown) {
                reminderDialogShown = true;
                showExpenseReminderDialog();
            }
        });
        viewModel.getBudgets().observe(this, budgets -> {
            monitor.onBudgetsUpdated(budgets);
        });

        // Load DashboardFragment by default
        if (savedInstanceState == null) {
            Fragment initialFragment = openExpenseLog
                    ? new ExpenseLogFragment()
                    : new DashboardFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, initialFragment)
                    .commit();

            // If opening expense log, also select the nav item
            if (openExpenseLog)
                bottomNav.setSelectedItemId(R.id.nav_expense);
        }

        // Handle navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = getFragmentForNavigation(item.getItemId());
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    private Fragment getFragmentForNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            android.widget.Toast.makeText(this, "Dashboard",
                    android.widget.Toast.LENGTH_SHORT).show();
            return new DashboardFragment();
        } else if (itemId == R.id.nav_expense) {
            android.widget.Toast.makeText(this, "Expense Log",
                    android.widget.Toast.LENGTH_SHORT).show();
            return new ExpenseLogFragment();
        } else if (itemId == R.id.nav_budgets) {
            android.widget.Toast.makeText(this, "Budgets",
                    android.widget.Toast.LENGTH_SHORT).show();
            return new BudgetListFragment();
        } else if (itemId == R.id.nav_chatbot) {
            android.widget.Toast.makeText(this, "Chatbot",
                    android.widget.Toast.LENGTH_SHORT).show();
            return new ChatbotFragment();
        } else if (itemId == R.id.nav_saving_circles) {
            android.widget.Toast.makeText(this, "Savings Circles",
                    android.widget.Toast.LENGTH_SHORT).show();
            return new SavingsCirclesFragment();
        }
        return null;
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


