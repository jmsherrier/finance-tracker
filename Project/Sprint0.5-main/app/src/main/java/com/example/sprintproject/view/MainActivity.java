package com.example.sprintproject.view;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;  // <-- you also need this for Intent
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.sprintproject.R;
import com.example.sprintproject.databinding.ActivityMainBinding;
import com.example.sprintproject.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        Button startBtn = findViewById(R.id.startBtn);
        Button quitBtn = findViewById(R.id.quitBtn);

        // ViewModel setup
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

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

        // Button actions
        startBtn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        quitBtn.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Quit app?")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", (d, which) -> finish())
                        .setNegativeButton("No", (d, which) -> d.dismiss())
                        .show()
        );
    }
}
