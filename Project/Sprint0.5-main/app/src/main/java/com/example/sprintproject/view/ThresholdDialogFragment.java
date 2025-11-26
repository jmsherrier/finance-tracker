package com.example.sprintproject.view;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.sprintproject.databinding.DialogThresholdBinding;
import com.example.sprintproject.model.ThresholdNotification;


public class ThresholdDialogFragment extends DialogFragment{
    public static final String ARG_TITLE = "arg_budget_title";
    public static final String ARG_THRESHOLD = "arg_threshold";
    public static final String ARG_PROGRESS = "arg_progress";

    private DialogThresholdBinding binding;
    private Runnable onComplete;

    public static ThresholdDialogFragment newInstance(ThresholdNotification n) {
        ThresholdDialogFragment f = new ThresholdDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, n.getBudgetTitle());
        b.putInt(ARG_THRESHOLD, n.getThresholdPercent());
        b.putDouble(ARG_PROGRESS, n.getProgress());
        f.setArguments(b);
        return f;
    }
    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogThresholdBinding.inflate(LayoutInflater.from(requireContext()));
        Bundle args = getArguments();
        String title = args != null ? args.getString(ARG_TITLE, "Budget"): "Budget";
        double progress = args != null ? args.getDouble(ARG_PROGRESS, 0.0) : 0.0;
        int percent = (int) Math.round(progress * 100);

        binding.title.setText(title + " budget");
        binding.message.setText("You've reached " + percent + "% of your " + title + " budget");
        binding.progressBar.setMax(100);
        binding.progressBar.setProgress(percent);

        binding.closeBtn.setOnClickListener(v -> dismiss());
        binding.dontShowBtn.setOnClickListener(v -> dismiss());

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(binding.getRoot()).create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (onComplete != null) {
            try { onComplete.run(); } catch (Exception ignored) {}
        }
        binding = null;
    }
}