package com.example.sprintproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sprintproject.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

// TODO: Future Enhancements for Registration
// 1. Enforce stronger email validation (regex for domain structure).
// 2. Disallow temporary/disposable email providers.
// 3. Require passwords with at least one uppercase, lowercase, digit, and symbol.
// 4. Implement a password strength meter to guide users interactively.
// 5. Add rate limiting or captcha to prevent bot signups.
// 6. Restrict weak/common passwords like "123456" or "password".
// 7. Add multi-factor authentication setup right after registration.
// 8. Prevent registration if email is already associated with a banned account.
// 9. Introduce a minimum/maximum password length policy.
// 10. Enforce Unicode normalization to avoid lookalike characters in emails.
// 11. Require confirm email field to reduce typos.
// 12. Consider region-specific restrictions (e.g., blocking certain domains).
// 13. Log failed registration attempts for monitoring suspicious activity.
// 14. Notify user via email verification before granting full access.
// 15. Keep all validation logic centralized for easy maintenance.

    private EditText emailField, passwordField, confirmPasswordField;
    private Button registerBtn, backToLoginBtn;
    private FirebaseAuth auth;

    private boolean looksLikeEmail(String e) {
        return e != null && e.contains("@") && e.contains(".");
    }

    private boolean looksLikePassword(String p) {
        return p != null && p.length() >= 6;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        registerBtn = findViewById(R.id.registerBtn);
        backToLoginBtn = findViewById(R.id.backToLoginBtn);

        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(v -> registerUser());
        backToLoginBtn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirm = confirmPasswordField.getText().toString().trim();

        boolean ok = true;

        if (!looksLikeEmail(email)) {
            emailField.setError("Enter a valid email");
            ok = false;
        }
        if (!looksLikePassword(password)) {
            passwordField.setError("Password must be at least 6 characters");
            ok = false;
        }
        if (!password.equals(confirm)) {
            confirmPasswordField.setError("Passwords do not match");
            ok = false;
        }
        if (!ok) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show();
            return;
        }


        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
