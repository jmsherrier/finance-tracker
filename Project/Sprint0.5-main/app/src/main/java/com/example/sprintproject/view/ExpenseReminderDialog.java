package com.example.sprintproject.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sprintproject.R;
import com.example.sprintproject.manager.ExpenseReminderManager;
import com.example.sprintproject.model.ExpenseReminder;

import java.util.Locale;

/**
 * DialogFragment that displays missed expense log reminders.
 * Shows when user hasn't logged expenses in a while.
 */
public class ExpenseReminderDialog extends DialogFragment {
    private static final String ARG_REMINDER = "reminder";

    private ExpenseReminder reminder;

    /**
     * Creates a new instance of ExpenseReminderDialog.
     *
     * @param reminder the expense reminder to display
     * @return new instance of ExpenseReminderDialog
     */
    public static ExpenseReminderDialog newInstance(ExpenseReminder reminder) {
        ExpenseReminderDialog fragment = new ExpenseReminderDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, reminder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reminder = (ExpenseReminder) getArguments().getSerializable(ARG_REMINDER);
        }
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_expense_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (reminder == null) {
            dismiss();
            return;
        }

        // Initialize views
        TextView textReminderMessage = view.findViewById(R.id.text_reminder_message);
        ImageView btnClose = view.findViewById(R.id.btn_close);
        Button btnLogExpense = view.findViewById(R.id.btn_log_expense);
        Button btnDismiss = view.findViewById(R.id.btn_dismiss);

        // Set reminder message
        String message = String.format(Locale.getDefault(),
                "It's been %d day%s since your last expense!",
                reminder.getDaysSinceLastExpense(),
                reminder.getDaysSinceLastExpense() == 1 ? "" : "s");
        textReminderMessage.setText(message);

        // Close button handler
        btnClose.setOnClickListener(v -> dismissReminder());

        // Dismiss button handler
        btnDismiss.setOnClickListener(v -> dismissReminder());

        // Log Expense button handler - navigate to expense log
        btnLogExpense.setOnClickListener(v -> {
            dismissReminder();
            navigateToExpenseLog();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Remove title from dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }
        return dialog;
    }

    /**
     * Dismisses the reminder and notifies the manager.
     */
    private void dismissReminder() {
        ExpenseReminderManager.getInstance().dismissCurrentReminder();
        dismiss();
    }

    /**
     * Navigates to the Expense Log fragment.
     */
    private void navigateToExpenseLog() {
        try {
            NavController navController = Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.navigation_expense_log);
        } catch (Exception e) {
            // If navigation fails, just dismiss
            dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog width match parent with some margin
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }
}
