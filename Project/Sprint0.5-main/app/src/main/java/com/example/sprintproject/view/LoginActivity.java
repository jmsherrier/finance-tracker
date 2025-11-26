package com.example.sprintproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sprintproject.R;
import com.example.sprintproject.manager.ExpenseReminderManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private Button loginBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private boolean looksLikeEmail(String e) {
        return e != null && e.contains("@") && e.contains(".");
    }

    private boolean looksLikePassword(String p) {
        return p != null && p.length() >= 6;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.registerBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize ExpenseReminderManager
        ExpenseReminderManager.getInstance().initialize(
                getApplicationContext(),
                db
        );

        loginBtn.setOnClickListener(v -> loginUser());
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        boolean ok = true;

        if (!looksLikeEmail(email)) {
            emailField.setError("Enter a valid email");
            ok = false;
        }
        if (!looksLikePassword(password)) {
            passwordField.setError("Password must be at least 6 characters");
            ok = false;
        }
        if (!ok) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show();
            return;
        }
        loginBtn.setEnabled(false);


        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    
                    // Check for missed expenses using the manager
                    String userId = auth.getCurrentUser() != null
                            ? auth.getCurrentUser().getUid() : null;
                    if (userId != null) {
                        ExpenseReminderManager.getInstance().checkMissedExpenses(userId);
                    }
                    
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    /**
     * Navigates to the Dashboard activity.
     */
    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

}
