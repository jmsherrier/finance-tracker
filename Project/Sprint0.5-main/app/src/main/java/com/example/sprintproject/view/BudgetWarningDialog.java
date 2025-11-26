package com.example.sprintproject.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sprintproject.R;
import com.example.sprintproject.manager.BudgetWarningManager;
import com.example.sprintproject.model.BudgetWarning;

import java.util.Locale;

/**
 * DialogFragment that displays budget warning notifications.
 * Shows when user spending reaches warning thresholds (80-90% of budget).
 */
public class BudgetWarningDialog extends DialogFragment {
    private static final String ARG_WARNING = "warning";

    private BudgetWarning warning;

    /**
     * Creates a new instance of BudgetWarningDialog.
     *
     * @param warning the budget warning to display
     * @return new instance of BudgetWarningDialog
     */
    public static BudgetWarningDialog newInstance(BudgetWarning warning) {
        BudgetWarningDialog fragment = new BudgetWarningDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WARNING, warning);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            warning = (BudgetWarning) getArguments().getSerializable(ARG_WARNING);
        }
        // Make dialog non-cancelable by touching outside
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_budget_warning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (warning == null) {
            dismiss();
            return;
        }

        // Initialize views
        TextView textWarningMessage = view.findViewById(R.id.text_warning_message);
        TextView textSpentAmount = view.findViewById(R.id.text_spent_amount);
        TextView textBudgetTotal = view.findViewById(R.id.text_budget_total);
        ProgressBar progressBudget = view.findViewById(R.id.progress_budget);
        TextView textPercentage = view.findViewById(R.id.text_percentage);
        ImageView btnClose = view.findViewById(R.id.btn_close);
        Button btnGotIt = view.findViewById(R.id.btn_got_it);

        // Set warning message
        String message = String.format(Locale.getDefault(),
                "You've reached %.0f%% of your %s budget",
                warning.getPercentage(),
                warning.getCategory());
        textWarningMessage.setText(message);

        // Set spent amount
        String spentText = String.format(Locale.getDefault(),
                "$%.2f", warning.getSpentAmount());
        textSpentAmount.setText(spentText);

        // Set budget total
        String totalText = String.format(Locale.getDefault(),
                "$%.2f", warning.getTotalAmount());
        textBudgetTotal.setText(totalText);

        // Set progress bar
        int progress = (int) warning.getPercentage();
        progressBudget.setProgress(progress);

        // Set percentage text
        String percentageText = String.format(Locale.getDefault(),
                "%.0f%% used", warning.getPercentage());
        textPercentage.setText(percentageText);

        // Close button handler
        btnClose.setOnClickListener(v -> dismissWarning());

        // Got It button handler
        btnGotIt.setOnClickListener(v -> dismissWarning());
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
     * Dismisses the warning and notifies the manager.
     */
    private void dismissWarning() {
        BudgetWarningManager.getInstance().dismissCurrentWarning();
        dismiss();
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
