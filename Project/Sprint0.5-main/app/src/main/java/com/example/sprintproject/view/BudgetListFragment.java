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
import com.example.sprintproject.adapter.UnifiedBudgetAdapter;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
    private SavingsCircleViewModel savingsCircleViewModel;

    private RecyclerView recyclerBudgets;
    private View textEmpty;
    private View textCount;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<Budget> budgets = new ArrayList<>();
    private List<SavingsCircle> circles = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private UnifiedBudgetAdapter adapter;

    private final String[] categories = {
        "Food & Dining", "Transportation", "Shopping", "Entertainment",
        "Bills & Utilities", "Healthcare", "Education", "Travel", "Other"
    };
    private final String[] frequencies = {"weekly", "monthly"};

    /**
     * Default constructor required for Fragment instantiation.
     * Fragments must have a public no-argument constructor.
     */
    public BudgetListFragment() {
        // Empty constructor - Fragment framework requires this
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_list, container, false);

        TimeViewModel timeViewModel = new ViewModelProvider(requireActivity()).get(
            TimeViewModel.class);
        savingsCircleViewModel = new ViewModelProvider(requireActivity()).get(
            SavingsCircleViewModel.class);

        timeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt =
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Log.d("ExpenseFragment", "Global date changed to " + fmt.format(date));
            reloadExpensesFor();
        });

        FirestoreManager fm = FirestoreManager.getInstance();
        db = fm.getDb();
        auth = fm.getAuth();

        recyclerBudgets = view.findViewById(R.id.recycler_budgets);
        FloatingActionButton fabAddBudget = view.findViewById(R.id.fab_add_budget);
        textEmpty = view.findViewById(R.id.text_empty_budgets);
        textCount = view.findViewById(R.id.budget_count);

        recyclerBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UnifiedBudgetAdapter(budgets, circles, expenses, 
            new UnifiedBudgetAdapter.OnItemClickListener() {
                @Override
                public void onBudgetClick(Budget budget) {
                    openBudgetDetails(budget);
                }

                @Override
                public void onCircleClick(SavingsCircle circle) {
                    openCircleDetails(circle);
                }
            });
        recyclerBudgets.setAdapter(adapter);

        fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        // Observe savings circles
        observeSavingsCircles();

        loadBudgets();
        loadExpenses();
        loadSavingsCircles();

        return view;
    }


    // Placeholder method for future date-based expense filtering
    private void reloadExpensesFor() {
        // Implementation pending - will filter expenses based on selected date
    }

    private String uid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
    }

    private void loadBudgets() {
        db.collection("users").document(uid()).collection("budgets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }
                    budgets.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Budget b = doc.toObject(Budget.class);
                        if (b == null) {
                            continue;
                        }
                        b.setId(doc.getId());
                        budgets.add(b);
                    }
                    updateUI();
                });
    }

    private void loadExpenses() {
        db.collection("expenses")
                .whereEqualTo("userId", uid())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }
                    expenses.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Expense ex = doc.toObject(Expense.class);
                        if (ex == null) {
                            continue;
                        }
                        ex.setId(doc.getId());
                        expenses.add(ex);
                    }
                    adapter.updateExpenses(expenses);
                });
    }

    private void observeSavingsCircles() {
        savingsCircleViewModel.getCircles().observe(getViewLifecycleOwner(), circlesList -> {
            if (circlesList != null) {
                circles = circlesList;
                adapter.updateCircles(circles);
                updateUI();
            }
        });
    }

    private void loadSavingsCircles() {
        savingsCircleViewModel.loadUserCircles();
    }

    private void updateUI() {
        adapter.updateBudgets(budgets);
        int totalItems = budgets.size() + circles.size();
        ((android.widget.TextView) textCount).setText(totalItems + " "
            + (totalItems == 1 ? "goal" : "goals"));

        if (budgets.isEmpty() && circles.isEmpty()) {
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
                                editAmount, AutoCompleteTextView dropdownCategory,
                                AutoCompleteTextView dropdownFrequency) {
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

        Budget budget = new Budget(titleVal, amountVal,
                categoryVal, frequencyVal, startDate, userId);

        db.collection("users").document(userId).collection("budgets")
                .add(budget)
                .addOnSuccessListener(ref ->
                        Toast.makeText(getContext(),
                                "Budget saved",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
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
        // Navigate to budget detail fragment
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

    private void openCircleDetails(SavingsCircle circle) {
        // Navigate to circle detail fragment
        CircleDetailFragment fragment = CircleDetailFragment.newInstance(circle.getId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}