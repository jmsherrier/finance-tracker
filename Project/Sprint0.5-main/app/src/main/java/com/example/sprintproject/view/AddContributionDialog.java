package com.example.sprintproject.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog for adding a contribution to a savings circle.
 */
public class AddContributionDialog {

    private final Context context;
    private final SavingsCircleViewModel viewModel;
    private final String circleId;

    /**
     * Constructor.
     * @param context The context
     * @param viewModel The view model
     * @param circleId The circle ID
     */
    public AddContributionDialog(Context context,
            SavingsCircleViewModel viewModel, String circleId) {
        this.context = context;
        this.viewModel = viewModel;
        this.circleId = circleId;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_contribution, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Initialize form fields
        TextInputEditText editAmount = dialogView.findViewById(R.id.edit_contribution_amount);
        TextInputEditText editNotes = dialogView.findViewById(R.id.edit_contribution_notes);
        TextInputEditText editDate = dialogView.findViewById(R.id.edit_contribution_date);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        // Setup date picker
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        editDate.setText(dateFormat.format(calendar.getTime()));

        editDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editDate.setText(dateFormat.format(calendar.getTime()));
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

        btnAdd.setOnClickListener(v -> {
            if (validateForm(editAmount, editDate)) {
                double amount = Double.parseDouble(editAmount.getText().toString().trim());
                String notes = editNotes.getText().toString().trim();
                Date date = calendar.getTime();

                viewModel.addContribution(circleId, amount, date, notes);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validateForm(TextInputEditText editAmount, TextInputEditText editDate) {
        boolean isValid = true;

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

        // Date is always valid since we set it by default
        String dateStr = editDate.getText().toString().trim();
        if (dateStr.isEmpty()) {
            editDate.setError("Date is required");
            isValid = false;
        } else {
            editDate.setError(null);
        }

        return isValid;
    }
}

