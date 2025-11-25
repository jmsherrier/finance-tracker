package com.example.sprintproject.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sprintproject.R;

/**
 * Dialog for notifying users when they haven't logged expenses recently.
 */
public class MissedExpenseDialog {

    /**
     * Listener for when user requests to log an expense.
     */
    public interface OnExpenseLogRequestedListener {
        /**
         * Called when user requests to log an expense.
         */
        void onExpenseLogRequested();
    }

    private final Context context;
    private final int daysSinceLastExpense;
    private final OnExpenseLogRequestedListener listener;

    /**
     * Creates a new MissedExpenseDialog.
     *
     * @param context The context to display the dialog in
     * @param daysSinceLastExpense Number of days since the last expense was logged
     * @param listener Listener for when user requests to log an expense
     */
    public MissedExpenseDialog(Context context, int daysSinceLastExpense,
                              OnExpenseLogRequestedListener listener) {
        this.context = context;
        this.daysSinceLastExpense = daysSinceLastExpense;
        this.listener = listener;
    }

    /**
     * Shows the missed expense dialog.
     */
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_missed_expense, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Initialize views
        TextView messageText = dialogView.findViewById(R.id.text_message);
        Button btnLogExpense = dialogView.findViewById(R.id.btn_log_expense);
        Button btnDismiss = dialogView.findViewById(R.id.btn_dismiss);

        // Set message with day count
        String message;
        if (daysSinceLastExpense == 1) {
            message = "You haven't logged any expenses in 1 day. "
                    + "Would you like to log your expenses now?";
        } else {
            message = "You haven't logged any expenses in " + daysSinceLastExpense
                    + " days. Would you like to log your expenses now?";
        }
        messageText.setText(message);

        // Button click listeners
        btnLogExpense.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onExpenseLogRequested();
            }
        });

        btnDismiss.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
