package com.example.sprintproject.view;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.sprintproject.R;
import com.example.sprintproject.databinding.ActivityMainBinding;
import com.example.sprintproject.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {
    private Button startBtn;
    private Button quitBtn;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        startBtn = findViewById(R.id.startBtn);
        quitBtn = findViewById(R.id.quitBtn);

        startBtn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

    }
        // Obtain ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe user changes
        authViewModel.getUser().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "User created: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            }
        });

        // Observe errors
        authViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        startBtn.setOnClickListener(v -> {
            // For template purposes, create a demo user
            authViewModel.createUser("demo@example.com", "password");
        });

        quitBtn.setOnClickListener(v -> finish());
    }
}