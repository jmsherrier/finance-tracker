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
    private static final String PREFS_NAME = "ExpenseTrackerPrefs";
    private static final String KEY_SESSION_CHECKED = "session_expense_check_done";
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
     * Checks if user has missed logging expenses and shows dialog if needed.
     */
    private void checkMissedExpenses() {
        String userId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            navigateToDashboard();
            return;
        }

        // Check if we've already shown the dialog this session
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alreadyChecked = prefs.getBoolean(KEY_SESSION_CHECKED, false);

        if (alreadyChecked) {
            navigateToDashboard();
            return;
        }

        // Query Firestore for last expense
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("expenses")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // User has never logged an expense - don't show dialog
                        navigateToDashboard();
                        return;
                    }

                    // Get the last expense date
                    QueryDocumentSnapshot lastExpenseDoc =
                            (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    Date lastExpenseDate = lastExpenseDoc.getDate("date");

                    if (lastExpenseDate != null) {
                        long daysSinceLastExpense = calculateDaysSince(lastExpenseDate);

                        if (daysSinceLastExpense > 0) {
                            // Mark that we've checked this session
                            prefs.edit().putBoolean(KEY_SESSION_CHECKED, true).apply();

                            // Show the dialog
                            MissedExpenseDialog dialog = new MissedExpenseDialog(
                                    this,
                                    (int) daysSinceLastExpense,
                                    () -> {
                                        Intent intent = new Intent(this, DashboardActivity.class);
                                        intent.putExtra("openExpenseLog", true);
                                        startActivity(intent);
                                        finish();
                                    }
                            );
                            dialog.show();
                        } else {
                            navigateToDashboard();
                        }
                    } else {
                        navigateToDashboard();
                    }
                })
                .addOnFailureListener(e -> {
                    // On error, just navigate to dashboard
                    navigateToDashboard();
                });
    }

    /**
     * Calculates the number of days since a given date.
     *
     * @param pastDate The date to calculate from
     * @return Number of complete days since the date
     */
    private long calculateDaysSince(Date pastDate) {
        long diffInMillis = new Date().getTime() - pastDate.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffInMillis);
    }

    /**
     * Navigates to the Dashboard activity.
     */
    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

}
