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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.R;
import com.example.sprintproject.adapter.ExpenseAdapter;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.repository.ExpenseRepository;
import com.example.sprintproject.viewmodel.ExpenseViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseLogFragment extends Fragment {
    private TimeViewModel timeViewModel;
    private ExpenseViewModel expenseViewModel;
    private FirebaseAuth auth;

    private RecyclerView recyclerView;
    private TextView emptyText;
    private TextView expenseCount;
    private FloatingActionButton fabAddExpense;
    private List<Expense> expenses = new ArrayList<>();
    private ExpenseAdapter expenseAdapter;

    // Categories for dropdown
    private final String[] categories = {
        "Food & Dining", "Transportation", "Shopping", "Entertainment", 
        "Bills & Utilities", "Healthcare", "Education", "Travel", "Other"
    };

    public ExpenseLogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_expense_log, container, false);

        timeViewModel = new ViewModelProvider(requireActivity()).get(TimeViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        auth = FirestoreManager.getInstance().getAuth();
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_expenses);
        emptyText = view.findViewById(R.id.text_empty);
        expenseCount = view.findViewById(R.id.expense_count);
        fabAddExpense = view.findViewById(R.id.fab_add_expense);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenses);
        recyclerView.setAdapter(expenseAdapter);
        
        // Observe expenses from ViewModel
        String userId = auth.getCurrentUser() != null 
            ? auth.getCurrentUser().getUid() : "anonymous";
        
        expenseViewModel.getExpenses(userId).observe(getViewLifecycleOwner(), expenseList -> {
            expenses.clear();
            if (expenseList != null) {
                expenses.addAll(expenseList);
            }
            expenseAdapter.updateExpenses(expenses);
            updateUI();
        });

        timeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt =
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Log.d("ExpenseFragment", "Global date changed to " + fmt.format(date));
        });
        
        // Setup FloatingActionButton
        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        
        return view;
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Initialize form fields
        TextInputEditText editName = dialogView.findViewById(R.id.edit_expense_name);
        TextInputEditText editAmount = dialogView.findViewById(R.id.edit_amount);
        AutoCompleteTextView dropdownCategory = dialogView.findViewById(R.id.dropdown_category);
        TextInputEditText editDate = dialogView.findViewById(R.id.edit_date);
        TextInputEditText editNotes = dialogView.findViewById(R.id.edit_notes);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        
        // Setup category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line, categories);
        dropdownCategory.setAdapter(categoryAdapter);
        
        // Setup date picker with future date restriction
        Calendar calendar = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        editDate.setText(dateFormat.format(calendar.getTime()));
        
        editDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            // Restrict future dates
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
        
        // Button click listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateForm(editName, editAmount, dropdownCategory, editDate)) {
                saveExpense(editName, editAmount, dropdownCategory, editDate, editNotes, calendar);
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private boolean validateForm(TextInputEditText editName, TextInputEditText editAmount, 
                               AutoCompleteTextView dropdownCategory, TextInputEditText editDate) {
        boolean isValid = true;
        
        // Validate name
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) {
            editName.setError("Expense name is required");
            isValid = false;
        } else {
            editName.setError(null);
        }
        
        // Validate amount
        String amountStr = editAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            editAmount.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    editAmount.setError("Amount must be greater than 0");
                    isValid = false;
                } else {
                    editAmount.setError(null);
                }
            } catch (NumberFormatException e) {
                editAmount.setError("Please enter a valid amount");
                isValid = false;
            }
        }
        
        // Validate category
        String category = dropdownCategory.getText().toString().trim();
        if (category.isEmpty()) {
            dropdownCategory.setError("Category is required");
            isValid = false;
        } else {
            dropdownCategory.setError(null);
        }
        
        // Date is always valid since we set it by default
        // Additional validation: ensure date is not in the future
        String dateStr = editDate.getText().toString().trim();
        if (!dateStr.isEmpty()) {
            try {
                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date selectedDate = dateFormat.parse(dateStr);
                if (selectedDate != null && selectedDate.after(new Date())) {
                    editDate.setError("Date cannot be in the future");
                    isValid = false;
                } else {
                    editDate.setError(null);
                }
            } catch (Exception e) {
                editDate.setError("Invalid date format");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void saveExpense(TextInputEditText editName, TextInputEditText editAmount,
                           AutoCompleteTextView dropdownCategory, TextInputEditText editDate,
                           TextInputEditText editNotes, Calendar calendar) {
        
        String name = editName.getText().toString().trim();
        double amount = Double.parseDouble(editAmount.getText().toString().trim());
        String category = dropdownCategory.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();
        Date date = calendar.getTime();
        String userId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : "anonymous";
        
        Expense expense = new Expense(name, amount, category, date, notes, userId);
        
        expenseViewModel.saveExpense(expense, new ExpenseRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Expense saved successfully!",
                        Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI() {
        expenseCount.setText(expenses.size() + " expenses");
        
        if (expenses.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

}

