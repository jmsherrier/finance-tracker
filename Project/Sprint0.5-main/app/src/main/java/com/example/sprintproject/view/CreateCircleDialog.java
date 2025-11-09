package com.example.sprintproject.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog for creating a new savings circle.
 */
public class CreateCircleDialog {
    
    public interface OnCircleCreatedListener {
        void onCircleCreated();
    }

    private final Context context;
    private final SavingsCircleViewModel viewModel;
    private final TimeViewModel timeViewModel;
    private final OnCircleCreatedListener listener;

    public CreateCircleDialog(Context context, SavingsCircleViewModel viewModel,
                             TimeViewModel timeViewModel, OnCircleCreatedListener listener) {
        this.context = context;
        this.viewModel = viewModel;
        this.timeViewModel = timeViewModel;
        this.listener = listener;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_create_circle, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Initialize form fields
        TextInputEditText editGroupName = dialogView.findViewById(R.id.edit_group_name);
        TextInputEditText editChallengeTitle = dialogView.findViewById(R.id.edit_challenge_title);
        TextInputEditText editGoalAmount = dialogView.findViewById(R.id.edit_goal_amount);
        AutoCompleteTextView dropdownFrequency = dialogView.findViewById(R.id.dropdown_frequency);
        TextInputEditText editNotes = dialogView.findViewById(R.id.edit_notes);
        TextInputEditText editStartDate = dialogView.findViewById(R.id.edit_start_date);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnCreate = dialogView.findViewById(R.id.btn_create);

        // Setup frequency dropdown
        String[] frequencies = {"Weekly", "Monthly"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line, frequencies);
        dropdownFrequency.setAdapter(frequencyAdapter);

        // Setup date picker - use Dashboard date selector
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        // Get date from TimeViewModel (Dashboard date selector)
        Date selectedDate = timeViewModel.getCurrentDate().getValue();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
            editStartDate.setText(dateFormat.format(selectedDate));
        } else {
            editStartDate.setText(dateFormat.format(calendar.getTime()));
        }

        editStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editStartDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Button click listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            if (validateForm(editGroupName, editChallengeTitle, editGoalAmount, 
                           dropdownFrequency, editStartDate)) {
                String groupName = editGroupName.getText().toString().trim();
                String challengeTitle = editChallengeTitle.getText().toString().trim();
                double goalAmount = Double.parseDouble(editGoalAmount.getText().toString().trim());
                String frequency = dropdownFrequency.getText().toString().trim().toLowerCase();
                String notes = editNotes.getText().toString().trim();
                Date startDate = calendar.getTime();

                viewModel.createCircle(groupName, challengeTitle, goalAmount, frequency, startDate, notes);
                dialog.dismiss();
                
                if (listener != null) {
                    listener.onCircleCreated();
                }
            }
        });

        dialog.show();
    }

    private boolean validateForm(TextInputEditText editGroupName,
                                TextInputEditText editChallengeTitle,
                                TextInputEditText editGoalAmount,
                                AutoCompleteTextView dropdownFrequency,
                                TextInputEditText editStartDate) {
        boolean isValid = true;

        // Validate group name
        String groupName = editGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            editGroupName.setError("Group name is required");
            isValid = false;
        } else {
            editGroupName.setError(null);
        }

        // Validate challenge title
        String challengeTitle = editChallengeTitle.getText().toString().trim();
        if (challengeTitle.isEmpty()) {
            editChallengeTitle.setError("Challenge title is required");
            isValid = false;
        } else {
            editChallengeTitle.setError(null);
        }

        // Validate goal amount
        String amountStr = editGoalAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            editGoalAmount.setError("Goal amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    editGoalAmount.setError("Goal amount must be greater than 0");
                    isValid = false;
                } else {
                    editGoalAmount.setError(null);
                }
            } catch (NumberFormatException e) {
                editGoalAmount.setError("Please enter a valid amount");
                isValid = false;
            }
        }

        // Validate frequency
        String frequency = dropdownFrequency.getText().toString().trim();
        if (frequency.isEmpty()) {
            dropdownFrequency.setError("Frequency is required");
            isValid = false;
        } else if (!"weekly".equalsIgnoreCase(frequency) && !"monthly".equalsIgnoreCase(frequency)) {
            dropdownFrequency.setError("Frequency must be 'Weekly' or 'Monthly'");
            isValid = false;
        } else {
            dropdownFrequency.setError(null);
        }

        // Date is always valid since we set it by default
        String dateStr = editStartDate.getText().toString().trim();
        if (dateStr.isEmpty()) {
            editStartDate.setError("Start date is required");
            isValid = false;
        } else {
            editStartDate.setError(null);
        }

        return isValid;
    }
}

