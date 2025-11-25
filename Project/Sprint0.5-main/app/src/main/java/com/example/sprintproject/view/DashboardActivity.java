package com.example.sprintproject.view;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        DashboardViewModel viewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.checkExpenseReminder();

        viewModel.getExpenseReminder().observe(this, shouldShow -> {
            if (shouldShow != null && shouldShow) {
                showExpenseReminderDialog();
            }
        });

        // Load DashboardFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
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
        new AlertDialog.Builder(this)
                .setTitle("Expense Reminder")
                .setMessage("You haven't logged any expenses recently. Would you like to update your expense log?")
                .setPositiveButton("Go to Expense Log", (dialog, which) -> {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ExpenseLogFragment())
                            .commit();
                })
                .setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .show();
    }
}


