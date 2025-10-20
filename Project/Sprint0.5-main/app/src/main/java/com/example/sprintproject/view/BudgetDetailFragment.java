package com.example.sprintproject.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.R;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.repository.BudgetRepository;
import com.example.sprintproject.viewmodel.BudgetViewModel;
import com.example.sprintproject.viewmodel.ExpenseViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows detailed info and utilization for a single budget.
 */
public class BudgetDetailFragment extends Fragment {
    private BudgetViewModel budgetViewModel;
    private ExpenseViewModel expenseViewModel;
    private FirebaseAuth auth;
    private String budgetId;
    private Budget budget;
    private List<Expense> expenses = new ArrayList<>();

    private TextInputEditText calcTotal, calcSpent, calcRemaining;
    private Button btnCalculate;

    private TextView textTitle;
    private TextView textCategoryFreq;
    private TextView textTarget;
    private TextView textRange;
    private TextView textUsed;
    private TextView textSurplus;
    private TextView textStatus;
    private LinearProgressIndicator progress;

    private final SimpleDateFormat fmt =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public BudgetDetailFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_budget_detail, container, false);

        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        auth = FirestoreManager.getInstance().getAuth();

        calcTotal = v.findViewById(R.id.calc_total);
        calcSpent = v.findViewById(R.id.calc_spent);
        calcRemaining = v.findViewById(R.id.calc_remaining);
        btnCalculate = v.findViewById(R.id.btn_calculate);
        btnCalculate.setOnClickListener(view -> calculateBudget());
        textTitle = v.findViewById(R.id.text_title);
        textCategoryFreq = v.findViewById(R.id.text_category_freq);
        textTarget = v.findViewById(R.id.text_target_amount);
        textRange = v.findViewById(R.id.text_date_range);
        textUsed = v.findViewById(R.id.text_used_amount);
        textSurplus = v.findViewById(R.id.text_surplus);
        textStatus = v.findViewById(R.id.text_status);
        progress = v.findViewById(R.id.progress_budget);

        if (getArguments() != null) {
            budgetId = getArguments().getString("budgetId");
        }

        String userId = uid();
        
        // Observe budget
        budgetViewModel.getBudget(userId, budgetId).observe(getViewLifecycleOwner(), b -> {
            if (b != null) {
                budget = b;
                loadExpenses();
            }
        });

        return v;
    }

    private String uid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
    }

    private void loadExpenses() {
        expenseViewModel.getExpenses(uid()).observe(getViewLifecycleOwner(), expenseList -> {
            expenses.clear();
            if (expenseList != null) {
                for (Expense e : expenseList) {
                    if (budget.getCategory().equalsIgnoreCase(e.getCategory())) {
                        expenses.add(e);
                    }
                }
            }
            updateUI();
        });
    }

    private void updateUI() {
        textTitle.setText(budget.getTitle());
        textCategoryFreq.setText(budget.getCategory() + " • " + budget.getFrequency());
        textTarget.setText(String.format("Budget: $%.2f", budget.getTotalAmount()));

        Date start = budget.getStartDate();
        Date end = getEndDate(budget);
        textRange.setText(fmt.format(start) + " - " + fmt.format(end));

        double used = calculateUsed(start, end);
        double remaining = Math.max(0, budget.getTotalAmount() - used);
        int percent =
                (int) Math.min(100, (used / Math.max(1e-9, budget.getTotalAmount())) * 100);

        textUsed.setText(String.format("Used: $%.2f", used));
        textSurplus.setText(String.format("Remaining: $%.2f", remaining));
        progress.setProgress(percent);

        String status;
        int color;
        boolean past = new Date().after(end);
        if (used >= budget.getTotalAmount()) {
            status = "Completed";
            color = getResources().getColor(android.R.color.holo_green_dark);
        } else if (past) {
            status = "Incomplete";
            color = getResources().getColor(android.R.color.holo_red_dark);
        } else {
            status = "In Progress";
            color = getResources().getColor(android.R.color.holo_orange_dark);
        }
        textStatus.setText("Status: " + status);
        textStatus.setTextColor(color);
    }

    private void calculateBudget() {
        String totalStr = calcTotal.getText().toString().trim();
        String spentStr = calcSpent.getText().toString().trim();
        String remainStr = calcRemaining.getText().toString().trim();

        // Count how many are filled
        int filled = 0;
        if (!totalStr.isEmpty()) {
            filled++;
        }
        if (!spentStr.isEmpty()) {
            filled++;
        }
        if (!remainStr.isEmpty()) {
            filled++;
        }

        // Need exactly 2
        if (filled != 2) {
            Toast.makeText(getContext(), "Fill exactly 2 fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate the missing one
        if (totalStr.isEmpty()) {
            double spent = Double.parseDouble(spentStr);
            double remaining = Double.parseDouble(remainStr);
            double total = spent + remaining;
            calcTotal.setText(String.valueOf(total));
            saveBudgetAmount(total, spent);
        } else if (spentStr.isEmpty()) {
            double total = Double.parseDouble(totalStr);
            double remaining = Double.parseDouble(remainStr);
            double spent = total - remaining;
            calcSpent.setText(String.valueOf(spent));
            saveBudgetAmount(total, spent);
        } else {
            double total = Double.parseDouble(totalStr);
            double spent = Double.parseDouble(spentStr);
            double remaining = total - spent;
            calcRemaining.setText(String.valueOf(remaining));
            saveBudgetAmount(total, spent);
        }
    }

    private void saveBudgetAmount(double total, double spent) {
        budgetViewModel.updateBudget(uid(), budgetId, total, spent,
            new BudgetRepository.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Budget updated!",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Error: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private double calculateUsed(Date start, Date end) {
        double total = 0;
        for (Expense e : expenses) {
            Date d = e.getDate();
            if (d.before(start) || d.after(end)) {
                continue;
            }
            total += e.getAmount();
        }
        return total;
    }

    private Date getEndDate(Budget b) {
        Calendar c = Calendar.getInstance();
        c.setTime(b.getStartDate());
        if ("weekly".equalsIgnoreCase(b.getFrequency())) {
            c.add(Calendar.DAY_OF_YEAR, 7);
        } else {
            c.add(Calendar.MONTH, 1);
        }
        return c.getTime();
    }
}
