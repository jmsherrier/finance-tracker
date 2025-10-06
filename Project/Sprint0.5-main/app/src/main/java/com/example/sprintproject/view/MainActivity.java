package com.example.sprintproject.view;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.sprintproject.R;
import com.example.sprintproject.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private Button startBtn;
    private Button quitBtn;

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


}