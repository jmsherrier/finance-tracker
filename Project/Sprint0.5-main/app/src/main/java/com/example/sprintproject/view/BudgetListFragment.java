package com.example.sprintproject.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.R;
import com.example.sprintproject.adapter.BudgetAdapter;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.repository.BudgetRepository;
import com.example.sprintproject.viewmodel.BudgetViewModel;
import com.example.sprintproject.viewmodel.ExpenseViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays all budgets, allows creation, and opens details on tap.
 */
public class BudgetListFragment extends Fragment {
    private TimeViewModel timeViewModel;
    private BudgetViewModel budgetViewModel;
    private ExpenseViewModel expenseViewModel;
    private FirebaseAuth auth;

    private RecyclerView recyclerBudgets;
    private FloatingActionButton fabAddBudget;
    private View textEmpty;
    private View textCount;

    private List<Budget> budgets = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private BudgetAdapter adapter;

    private final String[] categories = {
        "Food & Dining", "Transportation", "Shopping", "Entertainment",
        "Bills & Utilities", "Healthcare", "Education", "Travel", "Other"
    };
    private final String[] frequencies = {"weekly", "monthly"};

    public BudgetListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_list, container, false);

        timeViewModel = new ViewModelProvider(requireActivity()).get(TimeViewModel.class);
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        auth = FirestoreManager.getInstance().getAuth();

        recyclerBudgets = view.findViewById(R.id.recycler_budgets);
        fabAddBudget = view.findViewById(R.id.fab_add_budget);
        textEmpty = view.findViewById(R.id.text_empty_budgets);
        textCount = view.findViewById(R.id.budget_count);

        recyclerBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter(budgets, expenses, this::openBudgetDetails);
        recyclerBudgets.setAdapter(adapter);

        String userId = uid();
        
        // Observe budgets
        budgetViewModel.getBudgets(userId).observe(getViewLifecycleOwner(), budgetList -> {
            budgets.clear();
            if (budgetList != null) {
                budgets.addAll(budgetList);
            }
            updateUI();
        });
        
        // Observe expenses
        expenseViewModel.getExpenses(userId).observe(getViewLifecycleOwner(), expenseList -> {
            expenses.clear();
            if (expenseList != null) {
                expenses.addAll(expenseList);
            }
            adapter.notifyDataSetChanged();
        });

        fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        return view;
    }

    private String uid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        ((android.widget.TextView) textCount).setText(budgets.size() + " budgets");

        if (budgets.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerBudgets.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerBudgets.setVisibility(View.VISIBLE);
        }
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText editTitle = dialogView.findViewById(R.id.edit_title);
        TextInputEditText editAmount = dialogView.findViewById(R.id.edit_amount);
        AutoCompleteTextView dropdownCategory = dialogView.findViewById(R.id.dropdown_category);
        AutoCompleteTextView dropdownFrequency = dialogView.findViewById(R.id.dropdown_frequency);
        TextInputEditText editStartDate = dialogView.findViewById(R.id.edit_start_date);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        dialogView.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());

        dropdownCategory.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, categories));
        dropdownFrequency.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, frequencies));

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        editStartDate.setText(dateFormat.format(calendar.getTime()));

        editStartDate.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        editStartDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            if (validateForm(editTitle, editAmount, dropdownCategory, dropdownFrequency)) {
                saveBudget(editTitle, editAmount, dropdownCategory, dropdownFrequency, calendar);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validateForm(TextInputEditText editTitle, TextInputEditText
                                editAmount, AutoCompleteTextView dropdownCategory, AutoCompleteTextView dropdownFrequency) {
        boolean valid = true;

        if (editTitle.getText() == null || editTitle.getText().toString().trim().isEmpty()) {
            editTitle.setError("Title required");
            valid = false;
        }

        try {
            double val = Double.parseDouble(editAmount.getText().toString().trim());
            if (val < 0) {
                editAmount.setError("Amount must be non-negative");
                valid = false;
            }
        } catch (Exception e) {
            editAmount.setError("Enter valid number");
            valid = false;
        }

        if (dropdownCategory.getText() == null
                || dropdownCategory.getText().toString().trim().isEmpty()) {
            dropdownCategory.setError("Select category");
            valid = false;
        }

        if (dropdownFrequency.getText() == null
                || dropdownFrequency.getText().toString().trim().isEmpty()) {
            dropdownFrequency.setError("Select frequency");
            valid = false;
        }

        return valid;
    }

    private void saveBudget(TextInputEditText title, TextInputEditText amount,
                            AutoCompleteTextView category, AutoCompleteTextView frequency,
                            Calendar calendar) {

        String userId = uid();
        String titleVal = title.getText().toString().trim();
        double amountVal = Double.parseDouble(amount.getText().toString().trim());
        String categoryVal = category.getText().toString().trim();
        String frequencyVal = frequency.getText().toString().trim();
        Date startDate = normalizeStart(calendar.getTime(), frequencyVal);

        Budget budget = new Budget(titleVal, amountVal, categoryVal,
                frequencyVal, startDate, userId);

        budgetViewModel.saveBudget(budget, userId, new BudgetRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Budget saved",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Date normalizeStart(Date picked, String frequency) {
        Calendar c = Calendar.getInstance();
        c.setTime(picked);
        if ("weekly".equalsIgnoreCase(frequency)) {
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        } else {
            c.set(Calendar.DAY_OF_MONTH, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private void openBudgetDetails(Budget budget) {
        // Navigate to detail fragment
        BudgetDetailFragment fragment = new BudgetDetailFragment();
        Bundle args = new Bundle();
        args.putString("budgetId", budget.getId());
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}