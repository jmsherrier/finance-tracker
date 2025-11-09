package com.example.sprintproject.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

/**
 * Dialog for inviting a member to join a savings circle.
 */
public class InviteMemberDialog {

    private final Context context;
    private final SavingsCircleViewModel viewModel;
    private final String circleId;

    public InviteMemberDialog(Context context, SavingsCircleViewModel viewModel, String circleId) {
        this.context = context;
        this.viewModel = viewModel;
        this.circleId = circleId;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_invite_member, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Initialize form fields
        TextInputEditText editEmail = dialogView.findViewById(R.id.edit_invitee_email);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSend = dialogView.findViewById(R.id.btn_send);

        // Button click listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if (validateEmail(email)) {
                viewModel.sendInvitation(circleId, email);
                dialog.dismiss();
            } else {
                editEmail.setError("Please enter a valid email address");
            }
        });

        dialog.show();
    }

    private boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Simple email validation pattern
        Pattern emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );
        return emailPattern.matcher(email).matches();
    }
}

